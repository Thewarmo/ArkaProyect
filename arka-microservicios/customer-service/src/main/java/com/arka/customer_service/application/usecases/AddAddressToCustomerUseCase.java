package com.arka.customer_service.application.usecases;

import com.arka.customer_service.application.dto.AddressDTO;
import com.arka.customer_service.application.dto.CustomerResponse;
import com.arka.customer_service.application.ports.CustomerMapper;
import com.arka.customer_service.domain.entities.Address;
import com.arka.customer_service.domain.entities.Customer;
import com.arka.customer_service.domain.exceptions.CustomerNotFoundException;
import com.arka.customer_service.domain.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para agregar una dirección a un cliente
 */
@Service
@RequiredArgsConstructor
public class AddAddressToCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse execute(Long customerId, AddressDTO addressDTO) {
        // Buscar el cliente
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Crear la dirección
        Address address = Address.builder()
                .street(addressDTO.getStreet())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .postalCode(addressDTO.getPostalCode())
                .country(addressDTO.getCountry())
                .isDefault(addressDTO.isDefault())
                .build();

        // Agregar la dirección al cliente
        customer.addAddress(address);

        // Guardar
        Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(updatedCustomer);
    }
}
