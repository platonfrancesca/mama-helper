package com.example.mamahelper.controller.dto;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public record UpdateReminderRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 200) String description,
        @NotNull LocalDateTime time,
        RepeatPattern repeat,
        Set<DayOfWeek> days,
        LocalDate endDate
) {
    @AssertTrue(message = "weekly reminders need at least one day, other reminders must have no days")
    public boolean isDaysMatchRepeat() {
        return RepeatRules.daysMatchRepeat(repeatOrNone(), days);
    }

    @AssertTrue(message = "must not be before the first occurrence")
    public boolean isEndDateValid() {
        return RepeatRules.endDateNotBeforeTime(endDate, time);
    }

    public Reminder toEntity() {
        Reminder reminder = new Reminder();
        reminder.setTitle(title);
        reminder.setDescription(description);
        reminder.setTime(time);
        reminder.setRepeat(repeatOrNone());
        reminder.setDays(days == null ? new HashSet<>() : new HashSet<>(days));
        reminder.setEndDate(endDate);
        return reminder;
    }

    // repeat is optional in the JSON, so the mobile app's existing requests keep working
    private RepeatPattern repeatOrNone() {
        return repeat == null ? RepeatPattern.NONE : repeat;
    }
}
