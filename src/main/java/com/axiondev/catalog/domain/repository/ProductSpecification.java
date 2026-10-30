package com.axiondev.catalog.domain.repository;

import com.axiondev.catalog.domain.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Construtor de Queries Dinâmicas (JPA Criteria Specification) para o motor de Catálogo e Busca.
 * Permite combinar busca textual (nome/descrição/SKU), categoria, marca, faixa de preço e disponibilidade em estoque
 * em uma única consulta SQL parametrizada e protegida contra SQL Injection.
 */
public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> withDynamicFilters(
            String query,
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean onlyInStock
    ) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Sempre retorna apenas produtos ativos no catálogo público
            predicates.add(cb.isTrue(root.get("active")));

            // Busca textual case-insensitive em nome, descrição ou SKU
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate skuMatch = cb.like(cb.lower(root.get("sku")), pattern);
                predicates.add(cb.or(nameMatch, descMatch, skuMatch));
            }

            // Filtro exato por categoria
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("category")), category.trim().toUpperCase()));
            }

            // Filtro exato por marca
            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("brand")), brand.trim().toUpperCase()));
            }

            // Filtro por faixa de preço (mínimo e máximo)
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Filtro opcional de apenas itens com estoque disponível (> 0)
            if (Boolean.TRUE.equals(onlyInStock)) {
                predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
