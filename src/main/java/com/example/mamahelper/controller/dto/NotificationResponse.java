package com.example.mamahelper.controller.dto;

import com.example.mamahelper.entity.Notification;
import com.example.mamahelper.entity.Reminder;

import java.time.LocalDateTime;

/**
 * Only the reminder's id and title instead of the whole reminder (and its child).
 */
public record NotificationResponse(
        Long id,
        String message,
        LocalDateTime createdAt,
        boolean read,
        Long reminderId,
        String reminderTitle
) {
    public static NotificationResponse from(Notification notification) {
        Reminder reminder = notification.getReminder();

        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.isRead(),
                reminder == null ? null : reminder.getId(),
                reminder == null ? null : reminder.getTitle());
    }
}
