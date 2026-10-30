package com.axiondev.catalog.dto;

import com.axiondev.catalog.domain.entity.Product;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record imutável (Java 21) para transferência de dados (DTO) da camada de serviço para a API REST e Cache Redis.
 * Evita expor diretamente a entidade JPA e garante serialização limpa.
 */
public record ProductResponseDTO(
        Long id,
        String sku,
        String name,
        String description,
        String category,
        String brand,
        BigDecimal price,
        Integer stockQuantity,
        Boolean inStock,
        LocalDateTime createdAt
) implements Serializable {

    public static ProductResponseDTO fromEntity(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getBrand(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStockQuantity() != null && product.getStockQuantity() > 0,
                product.getCreatedAt()
        );
    }
}
