package com.arka.report_service.application.usecases;

import com.arka.report_service.application.dto.TopCustomerDTO;
import com.arka.report_service.application.dto.TopProductDTO;
import com.arka.report_service.application.dto.WeeklySalesReportResponse;
import com.arka.report_service.infrastructure.clients.CustomerServiceClient;
import com.arka.report_service.infrastructure.clients.OrderServiceClient;
import com.arka.report_service.infrastructure.clients.ProductServiceClient;
import com.arka.report_service.infrastructure.clients.dto.CustomerResponse;
import com.arka.report_service.infrastructure.clients.dto.OrderItemDTO;
import com.arka.report_service.infrastructure.clients.dto.OrderResponse;
import com.arka.report_service.infrastructure.clients.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateWeeklySalesReportUseCase {

    private final OrderServiceClient orderServiceClient;
    private final ProductServiceClient productServiceClient;
    private final CustomerServiceClient customerServiceClient;

    public WeeklySalesReportResponse execute(LocalDate startDate, LocalDate endDate) {
        log.info("Generating weekly sales report from {} to {}", startDate, endDate);

        // 1. Obtener todas las órdenes
        List<OrderResponse> allOrders = orderServiceClient.getAllOrders();
        log.debug("Total orders fetched: {}", allOrders.size());

        // 2. Filtrar órdenes por fecha
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<OrderResponse> weeklyOrders = allOrders.stream()
                .filter(order -> order.getCreatedAt().isAfter(startDateTime)
                        && order.getCreatedAt().isBefore(endDateTime))
                .collect(Collectors.toList());

        log.info("Orders in period: {}", weeklyOrders.size());

        // 3. Calcular total de ventas
        BigDecimal totalSales = weeklyOrders.stream()
                .map(OrderResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Calcular productos más vendidos
        List<TopProductDTO> topProducts = calculateTopProducts(weeklyOrders);

        // 5. Calcular clientes más frecuentes
        List<TopCustomerDTO> topCustomers = calculateTopCustomers(weeklyOrders);

        // 6. Calcular valor promedio de orden
        BigDecimal averageOrderValue = weeklyOrders.isEmpty() ? BigDecimal.ZERO :
                totalSales.divide(BigDecimal.valueOf(weeklyOrders.size()), 2, RoundingMode.HALF_UP);

        log.info("Report generated successfully: {} orders, {} total sales",
                weeklyOrders.size(), totalSales);

        return WeeklySalesReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalSales(totalSales)
                .totalOrders(weeklyOrders.size())
                .topProducts(topProducts)
                .topCustomers(topCustomers)
                .averageOrderValue(averageOrderValue)
                .build();
    }

    private List<TopProductDTO> calculateTopProducts(List<OrderResponse> orders) {
        // Agregar items de todas las órdenes
        Map<Long, ProductStats> productStatsMap = new HashMap<>();

        for (OrderResponse order : orders) {
            if (order.getItems() != null) {
                for (OrderItemDTO item : order.getItems()) {
                    productStatsMap.computeIfAbsent(item.getProductId(), k -> new ProductStats())
                            .addSale(item.getQuantity(), item.getSubtotal());
                }
            }
        }

        // Convertir a DTOs y ordenar por cantidad vendida
        return productStatsMap.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    ProductStats stats = entry.getValue();

                    // Obtener nombre del producto
                    String productName = "Producto " + productId;
                    ProductResponse product = productServiceClient.getProductById(productId);
                    if (product != null) {
                        productName = product.getName();
                    }

                    return TopProductDTO.builder()
                            .productId(productId)
                            .productName(productName)
                            .totalQuantitySold(stats.totalQuantity)
                            .totalRevenue(stats.totalRevenue)
                            .ordersCount(stats.ordersCount)
                            .build();
                })
                .sorted(Comparator.comparing(TopProductDTO::getTotalQuantitySold).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<TopCustomerDTO> calculateTopCustomers(List<OrderResponse> orders) {
        // Agregar órdenes por cliente
        Map<Long, CustomerStats> customerStatsMap = new HashMap<>();

        for (OrderResponse order : orders) {
            customerStatsMap.computeIfAbsent(order.getCustomerId(), k -> new CustomerStats())
                    .addOrder(order.getTotalAmount());
        }

        // Convertir a DTOs y ordenar por número de órdenes
        return customerStatsMap.entrySet().stream()
                .map(entry -> {
                    Long customerId = entry.getKey();
                    CustomerStats stats = entry.getValue();

                    // Obtener datos del cliente
                    String customerName = "Cliente " + customerId;
                    String companyName = "";
                    CustomerResponse customer = customerServiceClient.getCustomerById(customerId);
                    if (customer != null) {
                        customerName = customer.getContactName();
                        companyName = customer.getCompanyName();
                    }

                    BigDecimal avgOrderValue = stats.totalOrders > 0 ?
                            stats.totalSpent.divide(BigDecimal.valueOf(stats.totalOrders), 2, RoundingMode.HALF_UP) :
                            BigDecimal.ZERO;

                    return TopCustomerDTO.builder()
                            .customerId(customerId)
                            .customerName(customerName)
                            .companyName(companyName)
                            .totalOrders(stats.totalOrders)
                            .totalSpent(stats.totalSpent)
                            .averageOrderValue(avgOrderValue)
                            .build();
                })
                .sorted(Comparator.comparing(TopCustomerDTO::getTotalOrders).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    // Inner classes para stats
    private static class ProductStats {
        int totalQuantity = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int ordersCount = 0;

        void addSale(int quantity, BigDecimal revenue) {
            this.totalQuantity += quantity;
            this.totalRevenue = this.totalRevenue.add(revenue);
            this.ordersCount++;
        }
    }

    private static class CustomerStats {
        int totalOrders = 0;
        BigDecimal totalSpent = BigDecimal.ZERO;

        void addOrder(BigDecimal amount) {
            this.totalOrders++;
            this.totalSpent = this.totalSpent.add(amount);
        }
    }
}
