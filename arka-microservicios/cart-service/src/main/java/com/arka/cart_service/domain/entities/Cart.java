package com.arka.cart_service.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private String id;
    private Long customerId;
    private CartStatus status;
    private List<CartItem> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    public void addItem(CartItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }

        // Verificar si el producto ya existe en el carrito
        CartItem existingItem = this.items.stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Actualizar cantidad
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            existingItem.recalculateSubtotal();
        } else {
            // Agregar nuevo item
            item.setId(UUID.randomUUID().toString());
            item.recalculateSubtotal();
            this.items.add(item);
        }

        this.lastUpdatedAt = LocalDateTime.now();
        recalculateTotals();
    }

    public void removeItem(String itemId) {
        if (this.items != null) {
            this.items.removeIf(item -> item.getId().equals(itemId));
            this.lastUpdatedAt = LocalDateTime.now();
            recalculateTotals();
        }
    }

    public void updateItemQuantity(String itemId, Integer newQuantity) {
        if (this.items != null) {
            CartItem item = this.items.stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Item no encontrado en el carrito"));

            item.updateQuantity(newQuantity);
            this.lastUpdatedAt = LocalDateTime.now();
            recalculateTotals();
        }
    }

    public void clear() {
        if (this.items != null) {
            this.items.clear();
        }
        this.totalAmount = BigDecimal.ZERO;
        this.totalItems = 0;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void recalculateTotals() {
        if (this.items == null || this.items.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            this.totalItems = 0;
        } else {
            this.totalAmount = this.items.stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            this.totalItems = this.items.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        }
    }

    public void markAsAbandoned() {
        this.status = CartStatus.ABANDONED;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void markAsConverted() {
        this.status = CartStatus.CONVERTED;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public boolean isEmpty() {
        return this.items == null || this.items.isEmpty();
    }

    public boolean isActive() {
        return CartStatus.ACTIVE.equals(this.status);
    }
}
