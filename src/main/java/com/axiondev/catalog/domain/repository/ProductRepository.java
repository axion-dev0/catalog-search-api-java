package com.axiondev.catalog.domain.repository;

import com.axiondev.catalog.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Camada Repository de acesso a dados com Spring Data JPA.
 * Estende JpaRepository (CRUD e Paginação) e JpaSpecificationExecutor (filtros dinâmicos SQL para Catálogo e Busca).
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCase(String sku);
}
