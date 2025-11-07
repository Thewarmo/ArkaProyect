package com.arka.notification_service.infrastructure.persistence.repositories;

import com.arka.notification_service.domain.entities.NotificationStatus;
import com.arka.notification_service.domain.entities.NotificationType;
import com.arka.notification_service.infrastructure.persistence.model.NotificationJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationJPARepository extends JpaRepository<NotificationJPA, Long> {

    List<NotificationJPA> findByStatus(NotificationStatus status);

    List<NotificationJPA> findByType(NotificationType type);

    List<NotificationJPA> findByOrderId(Long orderId);

    List<NotificationJPA> findByRecipientEmail(String email);

    @Query("SELECT n FROM NotificationJPA n WHERE n.status = 'RETRY' AND n.retryCount < 3")
    List<NotificationJPA> findPendingRetries();

    long countByStatus(NotificationStatus status);
}
