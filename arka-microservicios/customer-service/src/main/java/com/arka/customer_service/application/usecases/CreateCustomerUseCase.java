package com.arka.customer_service.application.usecases;

import com.arka.customer_service.application.dto.CreateCustomerRequest;
import com.arka.customer_service.application.dto.CustomerResponse;
import com.arka.customer_service.application.ports.CustomerMapper;
import com.arka.customer_service.domain.entities.Customer;
import com.arka.customer_service.domain.exceptions.CustomerAlreadyExistsException;
import com.arka.customer_service.domain.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Caso de uso para crear un nuevo cliente
 */
@Service
@RequiredArgsConstructor
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse execute(CreateCustomerRequest request) {
        // Validar que no exista el userId
        if (customerRepository.existsByUserId(request.getUserId())) {
            throw new CustomerAlreadyExistsException("userId", request.getUserId().toString());
        }

        // Validar que no exista el email
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new CustomerAlreadyExistsException("email", request.getEmail());
        }

        // Validar que no exista el taxId
        if (customerRepository.existsByTaxId(request.getTaxId())) {
            throw new CustomerAlreadyExistsException("taxId", request.getTaxId());
        }

        // Crear el cliente
        Customer customer = Customer.builder()
                .userId(request.getUserId())
                .companyName(request.getCompanyName())
                .taxId(request.getTaxId())
                .contactName(request.getContactName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .country(request.getCountry())
                .addresses(new ArrayList<>())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Guardar el cliente
        Customer savedCustomer = customerRepository.save(customer);

        // Retornar respuesta
        return customerMapper.toResponse(savedCustomer);
    }
}
