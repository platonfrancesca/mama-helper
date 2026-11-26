package com.example.mamahelper.controller.dto;

import com.example.mamahelper.entity.Reminder;

import java.time.LocalDateTime;

/**
 * Same JSON shape as before (including the nested child), so the mobile app keeps working.
 */
public record ReminderResponse(
        Long id,
        String title,
        String description,
        LocalDateTime time,
        boolean notificationSent,
        ChildResponse child
) {
    public static ReminderResponse from(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getDescription(),
                reminder.getTime(),
                reminder.isNotificationSent(),
                reminder.getChild() == null ? null : ChildResponse.from(reminder.getChild()));
    }
}
