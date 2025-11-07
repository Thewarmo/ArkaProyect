package com.arka.report_service.infrastructure.clients;

import com.arka.report_service.infrastructure.clients.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceClient {

    @Qualifier("customerServiceWebClient")
    private final WebClient customerServiceWebClient;

    public CustomerResponse getCustomerById(Long customerId) {
        log.debug("Fetching customer {} from Customer Service", customerId);
        try {
            return customerServiceWebClient.get()
                    .uri("/api/v1/customers/{id}", customerId)
                    .retrieve()
                    .bodyToMono(CustomerResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching customer {} from Customer Service: {}", customerId, e.getMessage());
            return null; // Return null if customer not found, don't break report generation
        }
    }
}
