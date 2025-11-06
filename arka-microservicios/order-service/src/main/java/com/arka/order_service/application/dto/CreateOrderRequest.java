package com.arka.order_service.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "El customerId es obligatorio")
    private Long customerId;

    @NotEmpty(message = "La orden debe tener al menos un item")
    @Valid
    private List<OrderItemDTO> items;

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String shippingAddress;

    private String notes;
}
