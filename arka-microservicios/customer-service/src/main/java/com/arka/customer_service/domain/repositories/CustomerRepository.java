package com.arka.customer_service.domain.repositories;

import com.arka.customer_service.domain.entities.Country;
import com.arka.customer_service.domain.entities.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de clientes (Port en Clean Architecture).
 * Define las operaciones de persistencia sin depender de tecnologías específicas.
 */
public interface CustomerRepository {

    /**
     * Busca un cliente por su ID
     */
    Optional<Customer> findById(Long id);

    /**
     * Busca un cliente por su userId (del Auth Server)
     */
    Optional<Customer> findByUserId(Long userId);

    /**
     * Busca un cliente por su email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Busca un cliente por su Tax ID (NIT)
     */
    Optional<Customer> findByTaxId(String taxId);

    /**
     * Obtiene todos los clientes
     */
    List<Customer> findAll();

    /**
     * Obtiene clientes por país
     */
    List<Customer> findByCountry(Country country);

    /**
     * Obtiene clientes activos
     */
    List<Customer> findByActive(boolean active);

    /**
     * Guarda o actualiza un cliente
     */
    Customer save(Customer customer);

    /**
     * Elimina un cliente por su ID
     */
    void deleteById(Long id);

    /**
     * Verifica si existe un cliente con el userId dado
     */
    boolean existsByUserId(Long userId);

    /**
     * Verifica si existe un cliente con el email dado
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un cliente con el taxId dado
     */
    boolean existsByTaxId(String taxId);
}
