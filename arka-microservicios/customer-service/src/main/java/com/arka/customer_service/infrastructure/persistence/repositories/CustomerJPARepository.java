package com.arka.customer_service.infrastructure.persistence.repositories;

import com.arka.customer_service.domain.entities.Country;
import com.arka.customer_service.infrastructure.persistence.model.CustomerJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad CustomerJPA
 */
@Repository
public interface CustomerJPARepository extends JpaRepository<CustomerJPA, Long> {

    Optional<CustomerJPA> findByUserId(Long userId);

    Optional<CustomerJPA> findByEmail(String email);

    Optional<CustomerJPA> findByTaxId(String taxId);

    List<CustomerJPA> findByCountry(Country country);

    List<CustomerJPA> findByActive(boolean active);

    boolean existsByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByTaxId(String taxId);
}
