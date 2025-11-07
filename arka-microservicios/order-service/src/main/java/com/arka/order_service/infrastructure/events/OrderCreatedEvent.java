package com.arka.order_service.infrastructure.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {

    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private LocalDateTime createdAt;
}
