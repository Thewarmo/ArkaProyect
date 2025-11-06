package com.arka.auth_server.domain.repositories;

import com.arka.auth_server.domain.entities.User;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de usuarios (Port en Clean Architecture).
 * Define las operaciones de persistencia sin depender de tecnologías específicas.
 */
public interface UserRepository {

    /**
     * Busca un usuario por su ID
     */
    Optional<User> findById(Long id);

    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su email
     */
    Optional<User> findByEmail(String email);

    /**
     * Obtiene todos los usuarios
     */
    List<User> findAll();

    /**
     * Guarda o actualiza un usuario
     */
    User save(User user);

    /**
     * Elimina un usuario por su ID
     */
    void deleteById(Long id);

    /**
     * Verifica si existe un usuario con el username dado
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);
}
