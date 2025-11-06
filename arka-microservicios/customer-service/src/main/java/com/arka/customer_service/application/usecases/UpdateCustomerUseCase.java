package com.arka.customer_service.application.usecases;

import com.arka.customer_service.application.dto.CustomerResponse;
import com.arka.customer_service.application.dto.UpdateCustomerRequest;
import com.arka.customer_service.application.ports.CustomerMapper;
import com.arka.customer_service.domain.entities.Customer;
import com.arka.customer_service.domain.exceptions.CustomerNotFoundException;
import com.arka.customer_service.domain.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Caso de uso para actualizar un cliente
 */
@Service
@RequiredArgsConstructor
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse execute(Long customerId, UpdateCustomerRequest request) {
        // Buscar el cliente
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Actualizar solo los campos no nulos
        if (request.getCompanyName() != null) {
            customer.setCompanyName(request.getCompanyName());
        }
        if (request.getContactName() != null) {
            customer.setContactName(request.getContactName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getActive() != null) {
            customer.setActive(request.getActive());
        }

        customer.setUpdatedAt(LocalDateTime.now());

        // Guardar
        Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(updatedCustomer);
    }
}
