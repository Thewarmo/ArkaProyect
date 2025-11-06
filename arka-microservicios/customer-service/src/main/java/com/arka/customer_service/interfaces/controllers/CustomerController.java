package com.arka.customer_service.interfaces.controllers;

import com.arka.customer_service.application.dto.AddressDTO;
import com.arka.customer_service.application.dto.CreateCustomerRequest;
import com.arka.customer_service.application.dto.CustomerResponse;
import com.arka.customer_service.application.dto.UpdateCustomerRequest;
import com.arka.customer_service.application.usecases.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de clientes
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;
    private final GetCustomerByUserIdUseCase getCustomerByUserIdUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final GetAllCustomersUseCase getAllCustomersUseCase;
    private final AddAddressToCustomerUseCase addAddressToCustomerUseCase;

    /**
     * POST /api/v1/customers - Crear un nuevo cliente
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = createCustomerUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/customers/{id} - Obtener un cliente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        CustomerResponse response = getCustomerByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/customers/user/{userId} - Obtener un cliente por userId
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable Long userId) {
        CustomerResponse response = getCustomerByUserIdUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/customers - Obtener todos los clientes
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> response = getAllCustomersUseCase.execute();
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/customers/{id} - Actualizar un cliente
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse response = updateCustomerUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/customers/{id}/addresses - Agregar una dirección al cliente
     */
    @PostMapping("/{id}/addresses")
    public ResponseEntity<CustomerResponse> addAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        CustomerResponse response = addAddressToCustomerUseCase.execute(id, addressDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
