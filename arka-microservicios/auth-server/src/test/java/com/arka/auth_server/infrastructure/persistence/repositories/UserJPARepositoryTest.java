package com.arka.auth_server.infrastructure.persistence.repositories;

import com.arka.auth_server.domain.entities.Role;
import com.arka.auth_server.infrastructure.persistence.model.UserJPA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de Repositorio de Usuarios
 *
 * Este test demuestra:
 * - Operaciones CRUD de usuarios
 * - Constraints de unicidad (username, email)
 * - Búsquedas por username y email
 * - Verificación de existencia
 * - Roles y estados de habilitación
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserJPARepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("eureka.client.enabled", () -> "false");
    }

    @Autowired
    private UserJPARepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private UserJPA testUser;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testUser = UserJPA.builder()
                .username("testuser")
                .email("test@example.com")
                .password("$2a$10$hashedPassword") // BCrypt hash simulado
                .firstName("Test")
                .lastName("User")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
    }

    @Test
    void shouldSaveAndFindUserById() {
        // When
        UserJPA saved = repository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<UserJPA> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getFirstName()).isEqualTo("Test");
        assertThat(found.get().getLastName()).isEqualTo("User");
        assertThat(found.get().getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(found.get().isEnabled()).isTrue();
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByUsername() {
        // Given
        repository.save(testUser);
        entityManager.flush();

        // When
        Optional<UserJPA> found = repository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEmptyWhenUsernameNotFound() {
        // When
        Optional<UserJPA> found = repository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        repository.save(testUser);
        entityManager.flush();

        // When
        Optional<UserJPA> found = repository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<UserJPA> found = repository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckIfUsernameExists() {
        // Given
        repository.save(testUser);
        entityManager.flush();

        // When
        boolean exists = repository.existsByUsername("testuser");
        boolean notExists = repository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Given
        repository.save(testUser);
        entityManager.flush();

        // When
        boolean exists = repository.existsByEmail("test@example.com");
        boolean notExists = repository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldEnforceUniqueUsernameConstraint() {
        // Given
        repository.save(testUser);
        entityManager.flush();

        // When & Then - Intentar guardar otro usuario con el mismo username
        UserJPA duplicate = UserJPA.builder()
                .username("testuser") // Mismo username
                .email("different@example.com")
                .password("$2a$10$anotherHash")
                .firstName("Another")
                .lastName("User")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        assertThatThrownBy(() -> {
            repository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // DataIntegrityViolationException
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        // Given
        repository.save(testUser);
        entityManager.flush();

        // When & Then - Intentar guardar otro usuario con el mismo email
        UserJPA duplicate = UserJPA.builder()
                .username("differentuser")
                .email("test@example.com") // Mismo email
                .password("$2a$10$anotherHash")
                .firstName("Another")
                .lastName("User")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        assertThatThrownBy(() -> {
            repository.save(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // DataIntegrityViolationException
    }

    @Test
    void shouldSaveUserWithAdminRole() {
        // Given
        UserJPA adminUser = UserJPA.builder()
                .username("adminuser")
                .email("admin@example.com")
                .password("$2a$10$adminHash")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        // When
        UserJPA saved = repository.save(adminUser);
        entityManager.flush();

        // Then
        Optional<UserJPA> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldSaveDisabledUser() {
        // Given
        UserJPA disabledUser = UserJPA.builder()
                .username("disableduser")
                .email("disabled@example.com")
                .password("$2a$10$disabledHash")
                .firstName("Disabled")
                .lastName("User")
                .role(Role.CUSTOMER)
                .enabled(false) // Usuario deshabilitado
                .build();

        // When
        UserJPA saved = repository.save(disabledUser);
        entityManager.flush();

        // Then
        Optional<UserJPA> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isEnabled()).isFalse();
    }

    @Test
    void shouldUpdateUser() {
        // Given
        UserJPA saved = repository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        // When - Actualizar datos del usuario
        saved.setFirstName("Updated");
        saved.setLastName("Name");
        saved.setEnabled(false);
        UserJPA updated = repository.save(saved);
        entityManager.flush();

        // Then
        Optional<UserJPA> found = repository.findById(updated.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Updated");
        assertThat(found.get().getLastName()).isEqualTo("Name");
        assertThat(found.get().isEnabled()).isFalse();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldDeleteUser() {
        // Given
        UserJPA saved = repository.save(testUser);
        entityManager.flush();
        Long userId = saved.getId();

        // When
        repository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<UserJPA> deleted = repository.findById(userId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        // Given
        repository.save(testUser);

        UserJPA user2 = UserJPA.builder()
                .username("user2")
                .email("user2@example.com")
                .password("$2a$10$hash2")
                .firstName("Second")
                .lastName("User")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        repository.save(user2);

        UserJPA admin = UserJPA.builder()
                .username("admin")
                .email("admin@example.com")
                .password("$2a$10$adminHash")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        repository.save(admin);

        entityManager.flush();

        // When
        List<UserJPA> allUsers = repository.findAll();

        // Then
        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).extracting(UserJPA::getUsername)
                .containsExactlyInAnyOrder("testuser", "user2", "admin");
    }

    @Test
    void shouldHandleCaseSensitiveUsername() {
        // Given
        repository.save(testUser);
        entityManager.flush();

        // When
        Optional<UserJPA> exactMatch = repository.findByUsername("testuser");
        Optional<UserJPA> differentCase = repository.findByUsername("TestUser");

        // Then
        assertThat(exactMatch).isPresent();
        assertThat(differentCase).isEmpty(); // Username is case-sensitive
    }

    @Test
    void shouldStorePasswordHash() {
        // Given & When
        UserJPA saved = repository.save(testUser);
        entityManager.flush();

        // Then
        Optional<UserJPA> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getPassword()).startsWith("$2a$10$"); // BCrypt format
        assertThat(found.get().getPassword()).isNotEqualTo("plainPassword"); // Never store plain
    }

    @Test
    void shouldAutomaticallySetCreatedAt() {
        // When
        UserJPA saved = repository.save(testUser);
        entityManager.flush();

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
    }

    @Test
    void shouldAutomaticallyUpdateUpdatedAt() throws InterruptedException {
        // Given
        UserJPA saved = repository.save(testUser);
        entityManager.flush();
        entityManager.clear();

        java.time.LocalDateTime originalUpdatedAt = saved.getUpdatedAt();

        // Pequeña pausa para asegurar que el timestamp cambie
        Thread.sleep(10);

        // When - Actualizar usuario
        saved.setFirstName("Modified");
        UserJPA updated = repository.save(saved);
        entityManager.flush();

        // Then
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
    }
}