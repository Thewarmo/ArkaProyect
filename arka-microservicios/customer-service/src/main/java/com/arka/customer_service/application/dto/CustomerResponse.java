package com.arka.customer_service.application.dto;

import com.arka.customer_service.domain.entities.Country;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta con información del cliente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private Long userId;
    private String companyName;
    private String taxId;
    private String contactName;
    private String phone;
    private String email;
    private Country country;
    private List<AddressDTO> addresses;
    private boolean active;
    private boolean canPlaceOrders;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
