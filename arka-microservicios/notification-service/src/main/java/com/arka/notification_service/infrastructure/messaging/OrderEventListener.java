package com.arka.notification_service.infrastructure.messaging;

import com.arka.notification_service.application.dto.NotificationResponse;
import com.arka.notification_service.application.dto.OrderCreatedNotificationRequest;
import com.arka.notification_service.application.usecases.SendOrderCreatedNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final SendOrderCreatedNotificationUseCase sendOrderCreatedNotificationUseCase;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            log.info("Received order created event: orderNumber={}, orderId={}",
                    event.getOrderNumber(), event.getOrderId());

            // Convert event to notification request
            OrderCreatedNotificationRequest request = OrderCreatedNotificationRequest.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .customerEmail(event.getCustomerEmail())
                    .customerName(event.getCustomerName())
                    .totalAmount(event.getTotalAmount())
                    .shippingAddress(event.getShippingAddress())
                    .build();

            // Send notification (email)
            NotificationResponse response = sendOrderCreatedNotificationUseCase.execute(request);

            log.info("Order created notification processed successfully: notificationId={}, status={}",
                    response.getId(), response.getStatus());

        } catch (Exception e) {
            log.error("Failed to process order created event: orderNumber={}, error={}",
                    event.getOrderNumber(), e.getMessage(), e);
            // En producción, esto debería reencolar el mensaje o enviarlo a DLQ (Dead Letter Queue)
            throw e; // RabbitMQ reintentará automáticamente
        }
    }
}
