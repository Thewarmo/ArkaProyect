package com.arka.notification_service.application.usecases;

import com.arka.notification_service.application.dto.NotificationResponse;
import com.arka.notification_service.application.dto.OrderCreatedNotificationRequest;
import com.arka.notification_service.application.dto.SendNotificationRequest;
import com.arka.notification_service.domain.entities.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendOrderCreatedNotificationUseCase {

    private final SendNotificationUseCase sendNotificationUseCase;

    public NotificationResponse execute(OrderCreatedNotificationRequest request) {
        log.info("Preparing order created notification for order: {}", request.getOrderNumber());

        String subject = "¡Orden Confirmada! - " + request.getOrderNumber();
        String content = buildOrderCreatedEmailContent(request);

        SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                .type(NotificationType.ORDER_CREATED)
                .recipientEmail(request.getCustomerEmail())
                .recipientName(request.getCustomerName())
                .subject(subject)
                .content(content)
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .build();

        return sendNotificationUseCase.execute(notificationRequest);
    }

    private String buildOrderCreatedEmailContent(OrderCreatedNotificationRequest request) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        String formattedAmount = currencyFormat.format(request.getTotalAmount());

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; }
                        .order-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #4CAF50; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #777; }
                        .button { display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>¡Orden Confirmada!</h1>
                        </div>
                        <div class="content">
                            <p>Hola <strong>%s</strong>,</p>
                            <p>Tu orden ha sido recibida y está siendo procesada.</p>

                            <div class="order-details">
                                <h3>Detalles de la Orden</h3>
                                <p><strong>Número de orden:</strong> %s</p>
                                <p><strong>Total:</strong> %s</p>
                                <p><strong>Dirección de entrega:</strong> %s</p>
                            </div>

                            <p>Te enviaremos actualizaciones sobre el estado de tu orden por email.</p>
                            <p>Gracias por tu compra.</p>
                        </div>
                        <div class="footer">
                            <p>Arka - Distribuidora de Accesorios para PC</p>
                            <p>Este es un email automático, por favor no respondas.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                request.getCustomerName(),
                request.getOrderNumber(),
                formattedAmount,
                request.getShippingAddress()
        );
    }
}
