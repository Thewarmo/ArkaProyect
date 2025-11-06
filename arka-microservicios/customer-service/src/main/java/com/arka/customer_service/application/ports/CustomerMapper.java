package com.arka.customer_service.application.ports;

import com.arka.customer_service.application.dto.AddressDTO;
import com.arka.customer_service.application.dto.CustomerResponse;
import com.arka.customer_service.domain.entities.Address;
import com.arka.customer_service.domain.entities.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper entre entidades de dominio y DTOs de aplicación
 */
@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(customer.getUserId())
                .companyName(customer.getCompanyName())
                .taxId(customer.getTaxId())
                .contactName(customer.getContactName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .country(customer.getCountry())
                .addresses(toAddressDTOList(customer.getAddresses()))
                .active(customer.isActive())
                .canPlaceOrders(customer.canPlaceOrders())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public AddressDTO toAddressDTO(Address address) {
        if (address == null) {
            return null;
        }

        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .build();
    }

    public List<AddressDTO> toAddressDTOList(List<Address> addresses) {
        if (addresses == null) {
            return null;
        }

        return addresses.stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }
}
