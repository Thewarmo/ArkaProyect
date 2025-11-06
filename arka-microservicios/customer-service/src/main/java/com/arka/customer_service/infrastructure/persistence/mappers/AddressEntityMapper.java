package com.arka.customer_service.infrastructure.persistence.mappers;

import com.arka.customer_service.domain.entities.Address;
import com.arka.customer_service.infrastructure.persistence.model.AddressJPA;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Address (dominio) y AddressJPA (infraestructura)
 */
@Component
public class AddressEntityMapper {

    /**
     * Convierte de AddressJPA a Address (dominio)
     */
    public Address toDomain(AddressJPA addressJPA) {
        if (addressJPA == null) {
            return null;
        }

        return Address.builder()
                .id(addressJPA.getId())
                .street(addressJPA.getStreet())
                .city(addressJPA.getCity())
                .state(addressJPA.getState())
                .postalCode(addressJPA.getPostalCode())
                .country(addressJPA.getCountry())
                .isDefault(addressJPA.isDefault())
                .build();
    }

    /**
     * Convierte de Address (dominio) a AddressJPA
     */
    public AddressJPA toJPA(Address address) {
        if (address == null) {
            return null;
        }

        return AddressJPA.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .build();
    }
}
