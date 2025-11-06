package com.arka.auth_server.infrastructure.persistence.repositories;

import com.arka.auth_server.infrastructure.persistence.model.UserJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad UserJPA
 */
@Repository
public interface UserJPARepository extends JpaRepository<UserJPA, Long> {

    Optional<UserJPA> findByUsername(String username);

    Optional<UserJPA> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
