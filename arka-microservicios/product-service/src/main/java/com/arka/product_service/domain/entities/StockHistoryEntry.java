package com.arka.product_service.domain.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class StockHistoryEntry {

    private Long id;
    private Long productId;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private LocalDateTime changeDate;

    public StockHistoryEntry(Long productId, Integer previousStock, Integer newStock, String reason) {
        this.productId = productId;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.reason = reason;
        this.changeDate = LocalDateTime.now();
    }

    public Integer getStockDifference() {
        return newStock - previousStock;
    }

}
