package com.arka.customer_service.application.dto;

import com.arka.customer_service.domain.entities.Country;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de creación de un cliente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

    @NotNull(message = "El userId es obligatorio")
    private Long userId;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre de la empresa debe tener entre 3 y 200 caracteres")
    private String companyName;

    @NotBlank(message = "El Tax ID/NIT es obligatorio")
    @Size(min = 5, max = 50, message = "El Tax ID debe tener entre 5 y 50 caracteres")
    private String taxId;

    @NotBlank(message = "El nombre del contacto es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del contacto debe tener entre 3 y 100 caracteres")
    private String contactName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    private String phone;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotNull(message = "El país es obligatorio")
    private Country country;
}
