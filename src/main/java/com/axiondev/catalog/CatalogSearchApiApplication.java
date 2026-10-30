package com.axiondev.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Ponto de entrada da aplicação Spring Boot (equivalente ao main() do Dart/Flutter).
 * A anotação @EnableCaching ativa o suporte ao gerenciamento de cache distribuído com Redis.
 */
@SpringBootApplication
@EnableCaching
public class CatalogSearchApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogSearchApiApplication.class, args);
    }
}
