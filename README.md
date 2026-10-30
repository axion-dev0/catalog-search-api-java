# Catalog & Search API — Java 21 + Spring Boot 3 + PostgreSQL + Redis

API RESTful de **Catálogo e Busca de Alta Performance** desenvolvida em **Java 21** e **Spring Boot 3**, utilizando **Spring Data JPA (`JpaSpecificationExecutor`)** para filtros dinâmicos parametrizados no **PostgreSQL** e **Spring Data Redis** para cache distribuído de consultas frequentes.

---

## Arquitetura e Decisões Técnicas

O projeto aplica separação estrita de responsabilidades em camadas (**Controller → Service → Repository/Specification → Entity/DTO**):

1. **Busca Dinâmica e Segura (`ProductSpecification`):**
   - Implementa a API `Criteria / Specification` do **Spring Data JPA** para combinar filtros opcionais:
     - Busca textual (*case-insensitive*) por `name`, `description` ou `sku`;
     - Filtro exato por `category` e `brand`;
     - Filtro por faixa de preço (`minPrice` e `maxPrice`);
     - Filtro de disponibilidade imediata em estoque (`onlyInStock`);
     - Paginação e ordenação dinâmica (`Pageable` + `Sort`).
2. **Otimização SQL e Indexação (`PostgreSQL`):**
   - Índices compostos (`idx_products_category_active`) e índices dedicados (`idx_products_price`, `idx_products_sku`) declarados tanto no mapeamento JPA (`@Table(indexes = ...)`) quanto no script de inicialização [`data.sql`](src/main/resources/data.sql).
3. **Cache Distribuído e Invalidação Automática (`Redis`):**
   - Consultas de catálogo e busca por SKU utilizam `@Cacheable` com serialização JSON (`GenericJackson2JsonRedisSerializer`) e TTL configurável (10 minutos).
   - Operações de escrita (`POST /api/v1/catalog/products`) disparam `@CacheEvict(allEntries = true)` para manter consistência entre o PostgreSQL e o Redis.
4. **Imutabilidade e Validação (`Java 21 Records` + `Bean Validation`):**
   - Uso de `record` para DTOs de entrada e saída (`ProductCreateRequestDTO`, `ProductResponseDTO`, `CatalogPageResponseDTO`), desacoplando o modelo de domínio persistente da camada HTTP.

---

## Stack Tecnológica

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.3.x (`Spring Web`, `Spring Data JPA`, `Spring Data Redis`, `Spring Cache`, `Validation`)
- **Banco de Dados Relacional:** PostgreSQL 16
- **Cache em Memória:** Redis 7
- **Infraestrutura Local:** Docker & Docker Compose

---

## Como Executar Localmente

### 1. Subir PostgreSQL e Redis via Docker Compose
```bash
docker compose up -d
```

### 2. Executar a Aplicação Spring Boot
```bash
./mvnw spring-boot:run
```

---

## Principais Endpoints

### 1. Busca Paginada e Filtrada no Catálogo
```http
GET /api/v1/catalog/search?q=rtx&category=PLACA_DE_VIDEO&minPrice=2000&maxPrice=5000&onlyInStock=true&page=0&size=12&sortBy=price&direction=asc
```

### 2. Consulta Rápida por SKU (Cacheada no Redis)
```http
GET /api/v1/catalog/sku/HW-GPU-4070S
```

### 3. Cadastro de Produto no Catálogo (Com invalidação de Cache)
```http
POST /api/v1/catalog/products
Content-Type: application/json

{
  "sku": "HW-MB-B650M",
  "name": "Placa-Mãe ASUS TUF Gaming B650M-Plus",
  "description": "Socket AM5, DDR5, PCIe 5.0 M.2",
  "category": "PLACA_MAE",
  "brand": "ASUS",
  "price": 1399.90,
  "stockQuantity": 10
}
```

---

## Autor

**Daniel Getúlio Pereira Junior (AxionDev)**  
- GitHub: [@axion-dev0](https://github.com/axion-dev0)  
- E-mail Dev: `axiondev0@gmail.com` | E-mail Pessoal: `dnpereirajunior2@gmail.com`
