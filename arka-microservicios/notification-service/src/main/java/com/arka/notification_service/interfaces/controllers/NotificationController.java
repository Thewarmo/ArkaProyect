package com.arka.notification_service.interfaces.controllers;

import com.arka.notification_service.application.dto.NotificationResponse;
import com.arka.notification_service.application.dto.OrderCreatedNotificationRequest;
import com.arka.notification_service.application.dto.SendNotificationRequest;
import com.arka.notification_service.application.usecases.SendNotificationUseCase;
import com.arka.notification_service.application.usecases.SendOrderCreatedNotificationUseCase;
import com.arka.notification_service.domain.entities.Notification;
import com.arka.notification_service.domain.entities.NotificationStatus;
import com.arka.notification_service.domain.repositories.NotificationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final SendOrderCreatedNotificationUseCase sendOrderCreatedNotificationUseCase;
    private final NotificationRepository notificationRepository;

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.ok(sendNotificationUseCase.execute(request));
    }

    @PostMapping("/order-created")
    public ResponseEntity<NotificationResponse> sendOrderCreatedNotification(
            @Valid @RequestBody OrderCreatedNotificationRequest request) {
        return ResponseEntity.ok(sendOrderCreatedNotificationUseCase.execute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new com.arka.notification_service.domain.exceptions.NotificationNotFoundException(id));
        return ResponseEntity.ok(mapToResponse(notification));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        List<NotificationResponse> response = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByStatus(
            @PathVariable NotificationStatus status) {
        List<Notification> notifications = notificationRepository.findByStatus(status);
        List<NotificationResponse> response = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByOrderId(
            @PathVariable Long orderId) {
        List<Notification> notifications = notificationRepository.findByOrderId(orderId);
        List<NotificationResponse> response = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .recipientEmail(notification.getRecipientEmail())
                .recipientName(notification.getRecipientName())
                .subject(notification.getSubject())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .retryCount(notification.getRetryCount())
                .orderId(notification.getOrderId())
                .orderNumber(notification.getOrderNumber())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}
