package com.axiondev.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da aplicação Spring Boot.
 * @EnableCaching ativa o suporte ao gerenciamento de cache distribuído com Redis.
 * @EnableScheduling ativa rotinas agendadas (Jobs periódicos para limpeza de pedidos expirados).
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class CatalogSearchApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogSearchApiApplication.class, args);
    }
}
