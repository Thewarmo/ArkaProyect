package com.arka.customer_service.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de actualización de un cliente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    @Size(min = 3, max = 200, message = "El nombre de la empresa debe tener entre 3 y 200 caracteres")
    private String companyName;

    @Size(min = 3, max = 100, message = "El nombre del contacto debe tener entre 3 y 100 caracteres")
    private String contactName;

    @Size(min = 7, max = 20, message = "El teléfono debe tener entre 7 y 20 caracteres")
    private String phone;

    @Email(message = "El email debe ser válido")
    private String email;

    private Boolean active;
}
