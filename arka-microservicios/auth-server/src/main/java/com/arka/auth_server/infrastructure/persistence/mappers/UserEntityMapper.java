package com.arka.auth_server.infrastructure.persistence.mappers;

import com.arka.auth_server.domain.entities.User;
import com.arka.auth_server.infrastructure.persistence.model.UserJPA;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre User (dominio) y UserJPA (infraestructura)
 */
@Component
public class UserEntityMapper {

    /**
     * Convierte de UserJPA a User (dominio)
     */
    public User toDomain(UserJPA userJPA) {
        if (userJPA == null) {
            return null;
        }

        return User.builder()
                .id(userJPA.getId())
                .username(userJPA.getUsername())
                .email(userJPA.getEmail())
                .password(userJPA.getPassword())
                .firstName(userJPA.getFirstName())
                .lastName(userJPA.getLastName())
                .role(userJPA.getRole())
                .enabled(userJPA.isEnabled())
                .createdAt(userJPA.getCreatedAt())
                .updatedAt(userJPA.getUpdatedAt())
                .build();
    }

    /**
     * Convierte de User (dominio) a UserJPA
     */
    public UserJPA toJPA(User user) {
        if (user == null) {
            return null;
        }

        return UserJPA.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
