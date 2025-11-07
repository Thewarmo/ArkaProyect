package com.arka.report_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySalesReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalSales;
    private Integer totalOrders;
    private List<TopProductDTO> topProducts;
    private List<TopCustomerDTO> topCustomers;
    private BigDecimal averageOrderValue;
}
