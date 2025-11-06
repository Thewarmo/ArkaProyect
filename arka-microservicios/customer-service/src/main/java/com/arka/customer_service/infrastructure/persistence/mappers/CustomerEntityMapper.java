package com.arka.customer_service.infrastructure.persistence.mappers;

import com.arka.customer_service.domain.entities.Address;
import com.arka.customer_service.domain.entities.Customer;
import com.arka.customer_service.infrastructure.persistence.model.AddressJPA;
import com.arka.customer_service.infrastructure.persistence.model.CustomerJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Customer (dominio) y CustomerJPA (infraestructura)
 */
@Component
@RequiredArgsConstructor
public class CustomerEntityMapper {

    private final AddressEntityMapper addressMapper;

    /**
     * Convierte de CustomerJPA a Customer (dominio)
     */
    public Customer toDomain(CustomerJPA customerJPA) {
        if (customerJPA == null) {
            return null;
        }

        List<Address> addresses = customerJPA.getAddresses() != null
                ? customerJPA.getAddresses().stream()
                    .map(addressMapper::toDomain)
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return Customer.builder()
                .id(customerJPA.getId())
                .userId(customerJPA.getUserId())
                .companyName(customerJPA.getCompanyName())
                .taxId(customerJPA.getTaxId())
                .contactName(customerJPA.getContactName())
                .phone(customerJPA.getPhone())
                .email(customerJPA.getEmail())
                .country(customerJPA.getCountry())
                .addresses(addresses)
                .active(customerJPA.isActive())
                .createdAt(customerJPA.getCreatedAt())
                .updatedAt(customerJPA.getUpdatedAt())
                .build();
    }

    /**
     * Convierte de Customer (dominio) a CustomerJPA
     */
    public CustomerJPA toJPA(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerJPA customerJPA = CustomerJPA.builder()
                .id(customer.getId())
                .userId(customer.getUserId())
                .companyName(customer.getCompanyName())
                .taxId(customer.getTaxId())
                .contactName(customer.getContactName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .country(customer.getCountry())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();

        // Mapear direcciones manteniendo la relación bidireccional
        if (customer.getAddresses() != null) {
            customer.getAddresses().forEach(address -> {
                AddressJPA addressJPA = addressMapper.toJPA(address);
                customerJPA.addAddress(addressJPA);
            });
        }

        return customerJPA;
    }
}
