package com.example.mamahelper.controller.dto;

import com.example.mamahelper.entity.Reminder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateReminderRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 200) String description,
        @NotNull LocalDateTime time
) {
    public Reminder toEntity() {
        Reminder reminder = new Reminder();
        reminder.setTitle(title);
        reminder.setDescription(description);
        reminder.setTime(time);
        return reminder;
    }
}
