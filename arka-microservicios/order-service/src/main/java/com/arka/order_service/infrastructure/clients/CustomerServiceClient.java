package com.arka.order_service.infrastructure.clients;

import com.arka.order_service.infrastructure.clients.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceClient {

    @Qualifier("customerWebClient")
    private final WebClient customerWebClient;

    public CustomerResponse getCustomerById(Long customerId) {
        try {
            return customerWebClient.get()
                    .uri("/api/v1/customers/{id}", customerId)
                    .retrieve()
                    .bodyToMono(CustomerResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.error("Cliente no encontrado con ID: {}", customerId);
            throw new RuntimeException("Cliente no encontrado con ID: " + customerId);
        } catch (WebClientResponseException e) {
            log.error("Error al consultar cliente {}: {}", customerId, e.getMessage());
            throw new RuntimeException("No se pudo consultar el cliente " + customerId, e);
        }
    }

    public boolean customerExists(Long customerId) {
        try {
            CustomerResponse customer = getCustomerById(customerId);
            return customer != null;
        } catch (Exception e) {
            log.error("Error al verificar existencia del cliente {}: {}", customerId, e.getMessage());
            return false;
        }
    }

    public boolean customerCanPlaceOrders(Long customerId) {
        try {
            CustomerResponse customer = getCustomerById(customerId);
            return customer != null && customer.isActive() && customer.getDefaultAddress() != null;
        } catch (Exception e) {
            log.error("Error al verificar si el cliente {} puede hacer órdenes: {}", customerId, e.getMessage());
            return false;
        }
    }
}
