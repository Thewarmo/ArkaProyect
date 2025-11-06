package com.arka.order_service.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio que representa una orden de compra
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;
    private String orderNumber; // Número único de orden
    private Long customerId;
    private OrderStatus status;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalAmount;
    private String shippingAddress;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;

    /**
     * Calcula el total de la orden
     */
    public BigDecimal calculateTotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Agrega un item a la orden
     */
    public void addItem(OrderItem item) {
        items.add(item);
        recalculateTotal();
    }

    /**
     * Remueve un item de la orden
     */
    public void removeItem(Long itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
        recalculateTotal();
    }

    /**
     * Recalcula el total de la orden
     */
    public void recalculateTotal() {
        this.totalAmount = calculateTotal();
    }

    /**
     * Verifica si la orden puede ser modificada
     */
    public boolean canBeModified() {
        return status == OrderStatus.PENDING;
    }

    /**
     * Verifica si la orden puede ser cancelada
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    /**
     * Confirma la orden
     */
    public void confirm() {
        if (!canBeModified()) {
            throw new IllegalStateException("Solo las órdenes pendientes pueden ser confirmadas");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    /**
     * Cancela la orden
     */
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Esta orden no puede ser cancelada");
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Marca la orden como entregada
     */
    public void markAsDelivered() {
        if (status != OrderStatus.IN_TRANSIT) {
            throw new IllegalStateException("Solo las órdenes en tránsito pueden ser marcadas como entregadas");
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Verifica si la orden tiene items
     */
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    /**
     * Obtiene la cantidad total de productos en la orden
     */
    public Integer getTotalQuantity() {
        return items.stream()
                .map(OrderItem::getQuantity)
                .reduce(0, Integer::sum);
    }
}
