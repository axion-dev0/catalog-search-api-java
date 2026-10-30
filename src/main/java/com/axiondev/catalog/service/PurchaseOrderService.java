package com.axiondev.catalog.service;

import com.axiondev.catalog.domain.entity.Product;
import com.axiondev.catalog.domain.entity.PurchaseOrder;
import com.axiondev.catalog.domain.repository.ProductRepository;
import com.axiondev.catalog.domain.repository.PurchaseOrderRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de Gestão de Pedidos de Compra (Purchase Orders) com controle transacional de estoque
 * e rotina agendada (Scheduler / padrão Quartz) para limpeza de pedidos pendentes expirados.
 */
@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final ProductRepository productRepository;

    public PurchaseOrderService(PurchaseOrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    @CacheEvict(value = {"catalog_search", "product_by_sku"}, allEntries = true)
    public PurchaseOrder createOrder(String sku, int quantity, String buyerEmail) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade do pedido deve ser maior que zero");
        }

        Product product = productRepository.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado: " + sku));

        if (product.getStockQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Estoque insuficiente para o SKU: " + sku);
        }

        // Reserva o estoque de forma atômica na transação SQL
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        String orderNumber = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PurchaseOrder order = new PurchaseOrder(orderNumber, buyerEmail, product, quantity, total);
        return orderRepository.save(order);
    }

    /**
     * Job agendado (executado a cada 15 minutos) para cancelar pedidos PENDING expirados há mais de 24h
     * e devolver a reserva ao estoque automaticamente.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    @CacheEvict(value = {"catalog_search", "product_by_sku"}, allEntries = true)
    public int cancelExpiredPendingOrders() {
        LocalDateTime limit = LocalDateTime.now().minusHours(24);
        List<PurchaseOrder> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore("PENDING", limit);

        for (PurchaseOrder order : expiredOrders) {
            order.setStatus("EXPIRED_CANCELLED");
            Product product = order.getProduct();
            product.setStockQuantity(product.getStockQuantity() + order.getQuantity());
            productRepository.save(product);
        }

        orderRepository.saveAll(expiredOrders);
        return expiredOrders.size();
    }
}
