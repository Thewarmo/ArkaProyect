package com.arka.notification_service.domain.repositories;

import com.arka.notification_service.domain.entities.Notification;
import com.arka.notification_service.domain.entities.NotificationStatus;
import com.arka.notification_service.domain.entities.NotificationType;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findAll();

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByType(NotificationType type);

    List<Notification> findByOrderId(Long orderId);

    List<Notification> findByRecipientEmail(String email);

    List<Notification> findPendingRetries();

    long countByStatus(NotificationStatus status);
}
