package com.arka.customer_service.application.usecases;

import com.arka.customer_service.application.dto.CustomerResponse;
import com.arka.customer_service.application.ports.CustomerMapper;
import com.arka.customer_service.domain.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso para obtener todos los clientes
 */
@Service
@RequiredArgsConstructor
public class GetAllCustomersUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<CustomerResponse> execute() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }
}
