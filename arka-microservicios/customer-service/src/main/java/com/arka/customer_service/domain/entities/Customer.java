package com.arka.customer_service.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio que representa un cliente de Arka.
 * Esta clase es un POJO puro sin anotaciones de JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private Long id;
    private Long userId; // Referencia al usuario en Auth Server
    private String companyName; // Nombre de la empresa/almacén
    private String taxId; // NIT o identificación fiscal
    private String contactName; // Nombre del contacto principal
    private String phone;
    private String email;
    private Country country;

    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Obtiene la dirección por defecto del cliente
     */
    public Address getDefaultAddress() {
        return addresses.stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElse(addresses.isEmpty() ? null : addresses.get(0));
    }

    /**
     * Agrega una nueva dirección
     */
    public void addAddress(Address address) {
        if (addresses.isEmpty()) {
            address.setDefault(true);
        }
        addresses.add(address);
    }

    /**
     * Establece una dirección como predeterminada
     */
    public void setDefaultAddress(Long addressId) {
        addresses.forEach(addr -> addr.setDefault(addr.getId().equals(addressId)));
    }

    /**
     * Valida si el cliente puede realizar pedidos
     */
    public boolean canPlaceOrders() {
        return active && !addresses.isEmpty() && getDefaultAddress() != null;
    }

    /**
     * Verifica si tiene direcciones de entrega
     */
    public boolean hasAddresses() {
        return !addresses.isEmpty();
    }
}
