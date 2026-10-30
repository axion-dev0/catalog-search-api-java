package com.axiondev.catalog.service;

import com.axiondev.catalog.domain.entity.Product;
import com.axiondev.catalog.domain.repository.ProductRepository;
import com.axiondev.catalog.domain.repository.ProductSpecification;
import com.axiondev.catalog.dto.CatalogPageResponseDTO;
import com.axiondev.catalog.dto.ProductCreateRequestDTO;
import com.axiondev.catalog.dto.ProductResponseDTO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Camada de Regra de Negócio (Service) para Catálogo e Busca.
 * Combina consultas paginadas via JPA Specification no PostgreSQL com Cache distribuído no Redis.
 */
@Service
public class CatalogSearchService {

    private final ProductRepository productRepository;

    public CatalogSearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Busca paginada e filtrada no catálogo.
     * O resultado é armazenado em cache no Redis (@Cacheable) usando a combinação dos filtros como chave.
     */
    @Transactional(readOnly = true)
    @Cacheable(
            value = "catalog_search",
            key = "{#query, #category, #brand, #minPrice, #maxPrice, #onlyInStock, #page, #size, #sortBy, #direction}"
    )
    public CatalogPageResponseDTO<ProductResponseDTO> searchCatalog(
            String query,
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean onlyInStock,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, Math.min(size, 50), sort);

        Specification<Product> spec = ProductSpecification.withDynamicFilters(
                query, category, brand, minPrice, maxPrice, onlyInStock
        );

        Page<Product> resultPage = productRepository.findAll(spec, pageable);

        List<ProductResponseDTO> items = resultPage.getContent()
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();

        return new CatalogPageResponseDTO<>(
                items,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    /**
     * Consulta individual por SKU com cache dedicado no Redis.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "product_by_sku", key = "#sku.toUpperCase()")
    public ProductResponseDTO findBySku(String sku) {
        Product product = productRepository.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado para o SKU: " + sku
                ));
        return ProductResponseDTO.fromEntity(product);
    }

    /**
     * Cadastro de novo item no catálogo com invalidação automática (@CacheEvict) do cache de buscas no Redis,
     * garantindo consistência entre o PostgreSQL e o Redis.
     */
    @Transactional
    @CacheEvict(value = {"catalog_search", "product_by_sku"}, allEntries = true)
    public ProductResponseDTO createProduct(ProductCreateRequestDTO dto) {
        if (productRepository.existsBySkuIgnoreCase(dto.sku())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe um produto cadastrado com o SKU: " + dto.sku()
            );
        }

        Product product = new Product(
                dto.sku().trim().toUpperCase(),
                dto.name().trim(),
                dto.description(),
                dto.category().trim().toUpperCase(),
                dto.brand().trim().toUpperCase(),
                dto.price(),
                dto.stockQuantity()
        );

        Product saved = productRepository.save(product);
        return ProductResponseDTO.fromEntity(saved);
    }
}
