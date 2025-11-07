package com.arka.order_service.infrastructure.events;

import com.arka.order_service.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            log.info("Publishing order created event: orderNumber={}, orderId={}",
                    event.getOrderNumber(), event.getOrderId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                    event
            );

            log.info("Order created event published successfully: {}", event.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish order created event: orderNumber={}, error={}",
                    event.getOrderNumber(), e.getMessage(), e);
            // No lanzamos excepción para no bloquear la creación de la orden
            // La notificación se perderá pero la orden se crea correctamente
        }
    }
}
