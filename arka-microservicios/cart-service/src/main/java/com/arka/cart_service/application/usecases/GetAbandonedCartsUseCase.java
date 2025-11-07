package com.arka.cart_service.application.usecases;

import com.arka.cart_service.application.dto.AbandonedCartDTO;
import com.arka.cart_service.domain.entities.Cart;
import com.arka.cart_service.domain.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetAbandonedCartsUseCase {

    private final CartRepository cartRepository;
    private final WebClient customerServiceWebClient;

    @Value("${carts.abandoned.threshold-days:3}")
    private int thresholdDays;

    public List<AbandonedCartDTO> execute() {
        log.info("Finding abandoned carts (threshold: {} days)", thresholdDays);

        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(thresholdDays);
        List<Cart> abandonedCarts = cartRepository.findAbandonedCarts(thresholdDate);

        log.info("Found {} abandoned carts", abandonedCarts.size());

        return abandonedCarts.stream()
                .map(cart -> {
                    // Obtener información del cliente
                    CustomerInfo customerInfo = getCustomerInfo(cart.getCustomerId());

                    long daysSinceUpdate = ChronoUnit.DAYS.between(
                            cart.getLastUpdatedAt(),
                            LocalDateTime.now()
                    );

                    return AbandonedCartDTO.builder()
                            .cartId(cart.getId())
                            .customerId(cart.getCustomerId())
                            .customerName(customerInfo.name)
                            .customerEmail(customerInfo.email)
                            .totalItems(cart.getTotalItems())
                            .totalAmount(cart.getTotalAmount())
                            .lastUpdatedAt(cart.getLastUpdatedAt())
                            .daysSinceUpdate(daysSinceUpdate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private CustomerInfo getCustomerInfo(Long customerId) {
        try {
            CustomerResponse customer = customerServiceWebClient.get()
                    .uri("/api/v1/customers/{id}", customerId)
                    .retrieve()
                    .bodyToMono(CustomerResponse.class)
                    .block();

            if (customer != null) {
                return new CustomerInfo(customer.getContactName(), customer.getEmail());
            }
        } catch (Exception e) {
            log.error("Error fetching customer {}: {}", customerId, e.getMessage());
        }

        return new CustomerInfo("Cliente " + customerId, "");
    }

    // Inner classes
    private record CustomerInfo(String name, String email) {}

    private static class CustomerResponse {
        public String contactName;
        public String email;

        public String getContactName() { return contactName; }
        public String getEmail() { return email; }
    }
}
