package com.example.mamahelper.controller.dto;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Same JSON shape as before (including the nested child), so the mobile app keeps working.
 * {@code time} is the next occurrence; repeat, days and endDate describe the repeat pattern.
 */
public record ReminderResponse(
        Long id,
        String title,
        String description,
        LocalDateTime time,
        boolean notificationSent,
        ChildResponse child,
        RepeatPattern repeat,
        List<DayOfWeek> days,
        LocalDate endDate
) {
    public static ReminderResponse from(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getDescription(),
                reminder.getTime(),
                reminder.isNotificationSent(),
                reminder.getChild() == null ? null : ChildResponse.from(reminder.getChild()),
                reminder.getRepeat(),
                // sorted Monday to Sunday, so the app always gets the same order
                reminder.getDays() == null ? List.of() : reminder.getDays().stream().sorted().toList(),
                reminder.getEndDate());
    }
}
