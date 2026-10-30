package com.axiondev.catalog.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO de entrada para cadastro de novos itens no catálogo com validações declarativas (Bean Validation).
 */
public record ProductCreateRequestDTO(
        @NotBlank(message = "O SKU é obrigatório")
        @Size(max = 60)
        String sku,

        @NotBlank(message = "O nome do produto é obrigatório")
        @Size(max = 160)
        String name,

        String description,

        @NotBlank(message = "A categoria é obrigatória")
        @Size(max = 80)
        String category,

        @NotBlank(message = "A marca é obrigatória")
        @Size(max = 80)
        String brand,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        BigDecimal price,

        @NotNull(message = "A quantidade em estoque é obrigatória")
        @Min(value = 0, message = "O estoque não pode ser negativo")
        Integer stockQuantity
) {
}
