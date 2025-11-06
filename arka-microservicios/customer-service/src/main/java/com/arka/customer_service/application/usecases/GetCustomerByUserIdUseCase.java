package com.arka.customer_service.application.usecases;

import com.arka.customer_service.application.dto.CustomerResponse;
import com.arka.customer_service.application.ports.CustomerMapper;
import com.arka.customer_service.domain.entities.Customer;
import com.arka.customer_service.domain.exceptions.CustomerNotFoundException;
import com.arka.customer_service.domain.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para obtener un cliente por userId
 */
@Service
@RequiredArgsConstructor
public class GetCustomerByUserIdUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public CustomerResponse execute(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException("userId", userId.toString()));

        return customerMapper.toResponse(customer);
    }
}
