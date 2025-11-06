package com.arka.customer_service.application.dto;

import com.arka.customer_service.domain.entities.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para direcciones de entrega
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long id;

    @NotBlank(message = "La calle es obligatoria")
    @Size(min = 5, max = 200, message = "La calle debe tener entre 5 y 200 caracteres")
    private String street;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(min = 2, max = 100, message = "La ciudad debe tener entre 2 y 100 caracteres")
    private String city;

    @NotBlank(message = "El estado/departamento es obligatorio")
    @Size(min = 2, max = 100, message = "El estado debe tener entre 2 y 100 caracteres")
    private String state;

    @NotBlank(message = "El código postal es obligatorio")
    @Size(min = 3, max = 20, message = "El código postal debe tener entre 3 y 20 caracteres")
    private String postalCode;

    @NotNull(message = "El país es obligatorio")
    private Country country;

    @Builder.Default
    private boolean isDefault = false;
}
