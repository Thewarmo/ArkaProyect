package com.arka.product_service.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private Long brandId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
