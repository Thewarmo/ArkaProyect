package com.arka.report_service.infrastructure.clients;

import com.arka.report_service.infrastructure.clients.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderServiceClient {

    @Qualifier("orderServiceWebClient")
    private final WebClient orderServiceWebClient;

    public List<OrderResponse> getAllOrders() {
        log.debug("Fetching all orders from Order Service");
        try {
            return orderServiceWebClient.get()
                    .uri("/api/v1/orders")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<OrderResponse>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Error fetching orders from Order Service: {}", e.getMessage());
            throw new RuntimeException("Error al obtener órdenes del servicio: " + e.getMessage(), e);
        }
    }

    public OrderResponse getOrderById(Long orderId) {
        log.debug("Fetching order {} from Order Service", orderId);
        try {
            return orderServiceWebClient.get()
                    .uri("/api/v1/orders/{id}", orderId)
                    .retrieve()
                    .bodyToMono(OrderResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching order {} from Order Service: {}", orderId, e.getMessage());
            throw new RuntimeException("Error al obtener orden: " + e.getMessage(), e);
        }
    }
}
