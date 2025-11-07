package com.arka.order_service.application.usecases;

import com.arka.order_service.application.dto.*;
import com.arka.order_service.domain.entities.*;
import com.arka.order_service.domain.repositories.OrderRepository;
import com.arka.order_service.infrastructure.clients.CustomerServiceClient;
import com.arka.order_service.infrastructure.clients.InventoryServiceClient;
import com.arka.order_service.infrastructure.clients.ProductServiceClient;
import com.arka.order_service.infrastructure.clients.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final CustomerServiceClient customerServiceClient;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;

    @Transactional
    public OrderResponse execute(CreateOrderRequest request) {
        log.info("Iniciando creación de orden para cliente ID: {}", request.getCustomerId());

        // 1. Validar que el cliente existe y puede hacer órdenes
        validateCustomer(request.getCustomerId());

        // 2. Validar productos y obtener información actualizada
        validateAndEnrichProducts(request);

        // 3. Validar disponibilidad de stock
        validateStock(request);

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

        // 4. Reservar stock en el inventario
        reserveStockForOrder(savedOrder);

        log.info("Orden creada exitosamente: {}", savedOrder.getOrderNumber());

        return mapToResponse(savedOrder);
    }

    private void validateCustomer(Long customerId) {
        log.debug("Validando cliente ID: {}", customerId);
        if (!customerServiceClient.customerExists(customerId)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + customerId);
        }
        if (!customerServiceClient.customerCanPlaceOrders(customerId)) {
            throw new RuntimeException("El cliente no puede hacer órdenes. Verifique que esté activo y tenga una dirección de entrega configurada.");
        }
        log.debug("Cliente validado correctamente");
    }

    private void validateAndEnrichProducts(CreateOrderRequest request) {
        log.debug("Validando {} productos", request.getItems().size());
        for (OrderItemDTO item : request.getItems()) {
            ProductResponse product = productServiceClient.getProductById(item.getProductId());

            if (!product.isActive()) {
                throw new RuntimeException("El producto " + product.getName() + " no está disponible");
            }

            // Actualizar precio y nombre del producto con información actual
            item.setUnitPrice(product.getPrice());
            item.setProductName(product.getName());
        }
        log.debug("Productos validados y enriquecidos correctamente");
    }

    private void validateStock(CreateOrderRequest request) {
        log.debug("Validando disponibilidad de stock");
        for (OrderItemDTO item : request.getItems()) {
            if (!inventoryServiceClient.hasAvailableStock(item.getProductId(), item.getQuantity())) {
                throw new RuntimeException("Stock insuficiente para el producto: " + item.getProductName());
            }
        }
        log.debug("Stock validado correctamente");
    }

    private void reserveStockForOrder(Order order) {
        log.info("Reservando stock para orden: {}", order.getOrderNumber());
        try {
            for (OrderItem item : order.getItems()) {
                inventoryServiceClient.reserveStock(item.getProductId(), item.getQuantity());
                log.debug("Stock reservado: Producto={}, Cantidad={}", item.getProductId(), item.getQuantity());
            }
            log.info("Stock reservado exitosamente para la orden: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Error al reservar stock para la orden {}: {}", order.getOrderNumber(), e.getMessage());
            throw new RuntimeException("Error al reservar stock: " + e.getMessage(), e);
        }
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
