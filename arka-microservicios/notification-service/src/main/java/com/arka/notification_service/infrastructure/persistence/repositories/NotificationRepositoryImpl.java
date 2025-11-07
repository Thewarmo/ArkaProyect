package com.arka.notification_service.infrastructure.persistence.repositories;

import com.arka.notification_service.domain.entities.Notification;
import com.arka.notification_service.domain.entities.NotificationStatus;
import com.arka.notification_service.domain.entities.NotificationType;
import com.arka.notification_service.domain.repositories.NotificationRepository;
import com.arka.notification_service.infrastructure.persistence.mappers.NotificationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJPARepository jpaRepository;
    private final NotificationEntityMapper mapper;

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(jpaRepository.save(mapper.toJPA(notification)));
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByType(NotificationType type) {
        return jpaRepository.findByType(type).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByRecipientEmail(String email) {
        return jpaRepository.findByRecipientEmail(email).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findPendingRetries() {
        return jpaRepository.findPendingRetries().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByStatus(NotificationStatus status) {
        return jpaRepository.countByStatus(status);
    }
}
