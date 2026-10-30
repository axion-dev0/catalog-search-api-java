package com.axiondev.catalog.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Envelope de resposta paginada serializável para armazenamento direto no Redis Cache.
 */
public record CatalogPageResponseDTO<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) implements Serializable {
}
