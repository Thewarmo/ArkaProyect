package com.arka.customer_service.infrastructure.persistence.repositories;

import com.arka.customer_service.domain.entities.Country;
import com.arka.customer_service.domain.entities.Customer;
import com.arka.customer_service.domain.repositories.CustomerRepository;
import com.arka.customer_service.infrastructure.persistence.mappers.CustomerEntityMapper;
import com.arka.customer_service.infrastructure.persistence.model.CustomerJPA;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de clientes usando JPA
 */
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJPARepository jpaRepository;
    private final CustomerEntityMapper mapper;

    @Override
    public Optional<Customer> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByTaxId(String taxId) {
        return jpaRepository.findByTaxId(taxId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByCountry(Country country) {
        return jpaRepository.findByCountry(country).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Customer> findByActive(boolean active) {
        return jpaRepository.findByActive(active).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJPA customerJPA = mapper.toJPA(customer);
        CustomerJPA savedJPA = jpaRepository.save(customerJPA);
        return mapper.toDomain(savedJPA);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return jpaRepository.existsByTaxId(taxId);
    }
}
