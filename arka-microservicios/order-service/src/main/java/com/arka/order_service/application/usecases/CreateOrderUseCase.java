package com.arka.order_service.application.usecases;

import com.arka.order_service.application.dto.*;
import com.arka.order_service.domain.entities.*;
import com.arka.order_service.domain.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse execute(CreateOrderRequest request) {
        // Generar número de orden único
        String orderNumber = generateOrderNumber();

        // Crear items
        List<OrderItem> items = request.getItems().stream()
                .map(this::mapToOrderItem)
                .collect(Collectors.toList());

        // Crear orden
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .items(items)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        order.recalculateTotal();

        // Guardar orden
        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderItem mapToOrderItem(OrderItemDTO dto) {
        BigDecimal subtotal = dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        return OrderItem.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName() != null ? dto.getProductName() : "Product " + dto.getProductId())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .subtotal(subtotal)
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .items(order.getItems().stream().map(this::mapItemToDTO).collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .totalQuantity(order.getTotalQuantity())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .confirmedAt(order.getConfirmedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }

    private OrderItemDTO mapItemToDTO(OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
