package com.axiondev.catalog.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade JPA para Gestão de Pedidos de Compra (Purchase Order).
 * Demonstra relacionamento relacional (@ManyToOne) com Produto, controle de status e auditoria temporal.
 */
@Entity
@Table(
    name = "purchase_orders",
    indexes = {
        @Index(name = "idx_orders_status_created", columnList = "status, created_at"),
        @Index(name = "idx_orders_OrderNumber", columnList = "order_number", unique = true)
    }
)
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @Column(name = "buyer_email", nullable = false, length = 140)
    private String buyerEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PurchaseOrder() {
    }

    public PurchaseOrder(String orderNumber, String buyerEmail, Product product, Integer quantity, BigDecimal totalAmount) {
        this.orderNumber = orderNumber;
        this.buyerEmail = buyerEmail;
        this.product = product;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getBuyerEmail() { return buyerEmail; }
    public Product getProduct() { return product; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
