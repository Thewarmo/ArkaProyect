package com.arka.customer_service.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio que representa una dirección de entrega
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private Long id;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private Country country;
    private boolean isDefault;

    /**
     * Obtiene la dirección completa formateada
     */
    public String getFullAddress() {
        return String.format("%s, %s, %s %s, %s",
                street, city, state, postalCode, country.getName());
    }

    /**
     * Valida si la dirección está completa
     */
    public boolean isComplete() {
        return street != null && !street.isBlank() &&
               city != null && !city.isBlank() &&
               state != null && !state.isBlank() &&
               postalCode != null && !postalCode.isBlank() &&
               country != null;
    }
}
