package com.axiondev.catalog.service;

import com.axiondev.catalog.domain.entity.Product;
import com.axiondev.catalog.domain.entity.PurchaseOrder;
import com.axiondev.catalog.domain.repository.ProductRepository;
import com.axiondev.catalog.domain.repository.PurchaseOrderRepository;
import com.axiondev.catalog.dto.ProductCreateRequestDTO;
import com.axiondev.catalog.dto.ProductResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários automatizados com JUnit 5 e Mockito.
 * Valida regras de negócio de Catálogo e Gestão de Pedidos de Compra sem depender de banco externo.
 */
@ExtendWith(MockitoExtension.class)
class CatalogAndOrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseOrderRepository orderRepository;

    @InjectMocks
    private CatalogSearchService catalogSearchService;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    @Test
    @DisplayName("Deve cadastrar novo produto no catálogo quando o SKU for inédito")
    void shouldCreateProductWhenSkuIsUnique() {
        ProductCreateRequestDTO dto = new ProductCreateRequestDTO(
                "HW-GPU-4070S", "RTX 4070 Super", "GPU 12GB", "PLACA_DE_VIDEO", "NVIDIA",
                new BigDecimal("4199.90"), 10
        );

        when(productRepository.existsBySkuIgnoreCase("HW-GPU-4070S")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDTO response = catalogSearchService.createProduct(dto);

        assertNotNull(response);
        assertEquals("HW-GPU-4070S", response.sku());
        assertTrue(response.inStock());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Deve criar pedido de compra e abater estoque corretamente quando houver saldo")
    void shouldCreateOrderAndDeductStock() {
        Product product = new Product(
                "HW-CPU-7800X3D", "Ryzen 7 7800X3D", "CPU AM5", "PROCESSADOR", "AMD",
                new BigDecimal("2500.00"), 5
        );

        when(productRepository.findBySkuIgnoreCase("HW-CPU-7800X3D")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder order = purchaseOrderService.createOrder("HW-CPU-7800X3D", 2, "cliente@empresa.com");

        assertNotNull(order);
        assertEquals(3, product.getStockQuantity());
        assertEquals(new BigDecimal("5000.00"), order.getTotalAmount());
        assertEquals("PENDING", order.getStatus());
    }

    @Test
    @DisplayName("Deve bloquear pedido de compra quando a quantidade solicitada exceder o estoque")
    void shouldThrowExceptionWhenStockIsInsufficient() {
        Product product = new Product(
                "HW-SSD-990PRO", "SSD 2TB", "NVMe", "ARMAZENAMENTO", "SAMSUNG",
                new BigDecimal("1100.00"), 1
        );

        when(productRepository.findBySkuIgnoreCase("HW-SSD-990PRO")).thenReturn(Optional.of(product));

        assertThrows(ResponseStatusException.class, () ->
                purchaseOrderService.createOrder("HW-SSD-990PRO", 4, "cliente@empresa.com")
        );
        verify(orderRepository, never()).save(any());
    }
}
