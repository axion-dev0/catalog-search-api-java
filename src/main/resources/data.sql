-- Criação de índices otimizados no PostgreSQL para acelerar consultas de Catálogo e Busca
CREATE INDEX IF NOT EXISTS idx_products_category_active ON products (category, active);
CREATE INDEX IF NOT EXISTS idx_products_price ON products (price);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products (sku);

-- Carga inicial (Seed) idempotente para testes de Catálogo e Busca
INSERT INTO products (sku, name, description, category, brand, price, stock_quantity, active, created_at)
VALUES
  ('HW-GPU-4070S', 'Placa de Vídeo RTX 4070 Super 12GB GDDR6X', 'Arquitetura Ada Lovelace com DLSS 3 e Ray Tracing para alto desempenho.', 'PLACA_DE_VIDEO', 'NVIDIA', 4199.90, 8, true, CURRENT_TIMESTAMP),
  ('HW-CPU-7800X3D', 'Processador AMD Ryzen 7 7800X3D 8-Core 16-Threads', 'Tecnologia AMD 3D V-Cache de 96MB L3, socket AM5.', 'PROCESSADOR', 'AMD', 2899.00, 14, true, CURRENT_TIMESTAMP),
  ('HW-SSD-990PRO', 'SSD NVMe M.2 Samsung 990 PRO 2TB PCIe 4.0', 'Leitura sequencial de até 7450 MB/s e gravação de 6900 MB/s.', 'ARMAZENAMENTO', 'SAMSUNG', 1149.90, 22, true, CURRENT_TIMESTAMP),
  ('HW-RAM-DDR5-32', 'Memória RAM Corsair Vengeance 32GB (2x16GB) DDR5 6000MHz CL30', 'Compatível com perfis AMD EXPO e Intel XMP 3.0.', 'MEMORIA_RAM', 'CORSAIR', 899.90, 19, true, CURRENT_TIMESTAMP)
ON CONFLICT (sku) DO NOTHING;
