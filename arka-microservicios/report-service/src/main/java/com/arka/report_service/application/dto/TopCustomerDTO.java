package com.arka.report_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerDTO {
    private Long customerId;
    private String customerName;
    private String companyName;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal averageOrderValue;
}
