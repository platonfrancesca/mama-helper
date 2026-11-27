package com.example.mamahelper.controller.dto;

import com.example.mamahelper.entity.RepeatPattern;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Cross-field checks shared by the create and update requests.
 * They are used from {@code @AssertTrue} methods, so a broken rule becomes a normal 400 field error.
 */
final class RepeatRules {

    private RepeatRules() {
    }

    /** Weekly reminders need at least one day; other reminders must not have days. */
    static boolean daysMatchRepeat(RepeatPattern repeat, Set<DayOfWeek> days) {
        boolean hasDays = days != null && !days.isEmpty();
        return repeat == RepeatPattern.WEEKLY ? hasDays : !hasDays;
    }

    /** The end date can't be before the day of the first occurrence. */
    static boolean endDateNotBeforeTime(LocalDate endDate, LocalDateTime time) {
        return endDate == null || time == null || !endDate.isBefore(time.toLocalDate());
    }
}
