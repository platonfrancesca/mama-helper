package com.example.mamahelper.controller;

import com.example.mamahelper.controller.dto.NotificationResponse;
import com.example.mamahelper.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getAllNotifications() {
        return notificationService.getAllNotifications().stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(NotificationResponse.from(notificationService.getById(id)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(NotificationResponse.from(notificationService.markAsRead(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotificationById(@PathVariable Long id) {
        notificationService.deleteNotificationById(id);

        return ResponseEntity.noContent().build();
    }
}
