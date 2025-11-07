package com.arka.cart_service.infrastructure.persistence.model;

import com.arka.cart_service.domain.entities.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDocument {
    @Id
    private String id;

    @Indexed
    private Long customerId;

    @Indexed
    private CartStatus status;

    private List<CartItemDocument> items;
    private BigDecimal totalAmount;
    private Integer totalItems;

    @Indexed
    private LocalDateTime createdAt;

    @Indexed
    private LocalDateTime lastUpdatedAt;
}
