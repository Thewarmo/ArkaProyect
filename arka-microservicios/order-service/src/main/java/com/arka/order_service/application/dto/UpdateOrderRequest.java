package com.arka.order_service.application.dto;

import jakarta.validation.Valid;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {

    @Valid
    private List<OrderItemDTO> items;

    private String shippingAddress;

    private String notes;
}
