package com.arka.order_service.infrastructure.clients.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String country;
    private boolean active;
    private AddressDTO defaultAddress;
}
