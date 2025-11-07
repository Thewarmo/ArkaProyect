package com.arka.order_service.application.usecases;

import com.arka.order_service.application.dto.OrderResponse;
import com.arka.order_service.domain.entities.Order;
import com.arka.order_service.domain.entities.OrderItem;
import com.arka.order_service.domain.entities.OrderStatus;
import com.arka.order_service.domain.exceptions.OrderCannotBeModifiedException;
import com.arka.order_service.domain.exceptions.OrderNotFoundException;
import com.arka.order_service.domain.repositories.OrderRepository;
import com.arka.order_service.infrastructure.clients.InventoryServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final GetOrderByIdUseCase getOrderByIdUseCase;

    @Transactional
    public OrderResponse execute(Long orderId) {
        log.info("Iniciando cancelación de orden ID: {}", orderId);

        // Buscar la orden
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada con ID: " + orderId));

        // Validar que la orden puede ser cancelada (solo PENDING y CONFIRMED)
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new OrderCannotBeModifiedException(order.getStatus());
        }

        // Liberar stock reservado
        releaseStockForOrder(order);

        // Cancelar la orden
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        // Guardar cambios
        Order cancelledOrder = orderRepository.save(order);

        log.info("Orden cancelada exitosamente: {}", cancelledOrder.getOrderNumber());

        // Retornar respuesta usando el caso de uso de consulta
        return getOrderByIdUseCase.execute(cancelledOrder.getId());
    }

    private void releaseStockForOrder(Order order) {
        log.info("Liberando stock reservado para orden: {}", order.getOrderNumber());
        try {
            for (OrderItem item : order.getItems()) {
                inventoryServiceClient.releaseStock(item.getProductId(), item.getQuantity());
                log.debug("Stock liberado: Producto={}, Cantidad={}", item.getProductId(), item.getQuantity());
            }
            log.info("Stock liberado exitosamente para la orden: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Error al liberar stock para la orden {}: {}", order.getOrderNumber(), e.getMessage());
            // No lanzamos excepción para no bloquear la cancelación
            // La orden se cancela pero el stock podría quedar reservado
            // Se requiere un proceso manual o automático de reconciliación
            log.warn("La orden será cancelada pero el stock podría quedar reservado. Requiere reconciliación manual.");
        }
    }
}
