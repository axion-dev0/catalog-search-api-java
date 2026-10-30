package com.axiondev.catalog.controller;

import com.axiondev.catalog.dto.CatalogPageResponseDTO;
import com.axiondev.catalog.dto.ProductCreateRequestDTO;
import com.axiondev.catalog.dto.ProductResponseDTO;
import com.axiondev.catalog.service.CatalogSearchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controller REST responsável por expor os endpoints HTTP de Catálogo e Busca.
 */
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogSearchController {

    private final CatalogSearchService catalogSearchService;

    public CatalogSearchController(CatalogSearchService catalogSearchService) {
        this.catalogSearchService = catalogSearchService;
    }

    /**
     * GET /api/v1/catalog/search
     * Endpoint de busca combinada com filtros dinâmicos, ordenação, paginação e cache Redis.
     */
    @GetMapping("/search")
    public ResponseEntity<CatalogPageResponseDTO<ProductResponseDTO>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyInStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        CatalogPageResponseDTO<ProductResponseDTO> response = catalogSearchService.searchCatalog(
                q, category, brand, minPrice, maxPrice, onlyInStock, page, size, sortBy, direction
        );
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/catalog/sku/{sku}
     * Busca rápida por código SKU com cache em memória (Redis).
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponseDTO> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(catalogSearchService.findBySku(sku));
    }

    /**
     * POST /api/v1/catalog/products
     * Criação de produto e limpeza automática do cache de catálogo.
     */
    @PostMapping("/products")
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductCreateRequestDTO request) {
        ProductResponseDTO created = catalogSearchService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
