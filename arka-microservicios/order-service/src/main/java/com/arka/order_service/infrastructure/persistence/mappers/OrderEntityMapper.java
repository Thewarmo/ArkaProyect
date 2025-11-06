package com.arka.order_service.infrastructure.persistence.mappers;

import com.arka.order_service.domain.entities.*;
import com.arka.order_service.infrastructure.persistence.model.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderEntityMapper {

    public Order toDomain(OrderJPA jpa) {
        if (jpa == null) return null;

        return Order.builder()
                .id(jpa.getId())
                .orderNumber(jpa.getOrderNumber())
                .customerId(jpa.getCustomerId())
                .status(jpa.getStatus())
                .items(jpa.getItems().stream().map(this::itemToDomain).collect(Collectors.toList()))
                .totalAmount(jpa.getTotalAmount())
                .shippingAddress(jpa.getShippingAddress())
                .notes(jpa.getNotes())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .confirmedAt(jpa.getConfirmedAt())
                .deliveredAt(jpa.getDeliveredAt())
                .build();
    }

    public OrderJPA toJPA(Order domain) {
        if (domain == null) return null;

        OrderJPA jpa = OrderJPA.builder()
                .id(domain.getId())
                .orderNumber(domain.getOrderNumber())
                .customerId(domain.getCustomerId())
                .status(domain.getStatus())
                .totalAmount(domain.getTotalAmount())
                .shippingAddress(domain.getShippingAddress())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .confirmedAt(domain.getConfirmedAt())
                .deliveredAt(domain.getDeliveredAt())
                .build();

        domain.getItems().forEach(item -> {
            OrderItemJPA itemJPA = itemToJPA(item);
            jpa.addItem(itemJPA);
        });

        return jpa;
    }

    private OrderItem itemToDomain(OrderItemJPA jpa) {
        return OrderItem.builder()
                .id(jpa.getId())
                .productId(jpa.getProductId())
                .productName(jpa.getProductName())
                .quantity(jpa.getQuantity())
                .unitPrice(jpa.getUnitPrice())
                .subtotal(jpa.getSubtotal())
                .build();
    }

    private OrderItemJPA itemToJPA(OrderItem domain) {
        return OrderItemJPA.builder()
                .id(domain.getId())
                .productId(domain.getProductId())
                .productName(domain.getProductName())
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice())
                .subtotal(domain.getSubtotal())
                .build();
    }
}
