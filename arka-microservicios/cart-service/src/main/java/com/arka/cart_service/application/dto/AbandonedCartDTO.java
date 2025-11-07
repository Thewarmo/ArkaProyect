package com.arka.cart_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbandonedCartDTO {
    private String cartId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private LocalDateTime lastUpdatedAt;
    private Long daysSinceUpdate;
}
