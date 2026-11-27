package com.example.mamahelper.service;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Works out the occurrences of a reminder from its repeat pattern (see ADR 0001).
 * Pure logic with no database or clock, so it can be tested with plain JUnit.
 */
public final class NextOccurrenceCalculator {

    private NextOccurrenceCalculator() {
    }

    /**
     * The first occurrence strictly after {@code after}, at the reminder's time of day.
     * Empty when the reminder doesn't repeat, or the occurrence would be past the end date.
     */
    public static Optional<LocalDateTime> nextOccurrenceAfter(Reminder reminder, LocalDateTime after) {
        LocalTime timeOfDay = reminder.getTime().toLocalTime();

        // A daily or weekly reminder always has an occurrence within the next 8 days
        for (int i = 0; i <= 7; i++) {
            LocalDateTime candidate = after.toLocalDate().plusDays(i).atTime(timeOfDay);

            if (candidate.isAfter(after) && occursOn(reminder, candidate.toLocalDate())) {
                return isOnOrBeforeEndDate(reminder, candidate) ? Optional.of(candidate) : Optional.empty();
            }
        }
        return Optional.empty(); // NONE, or WEEKLY with no days
    }

    /**
     * The first occurrence on or after {@code start}. Used when a reminder is created or edited,
     * so a weekly reminder lands on one of its chosen days. A one-time reminder keeps {@code start}.
     */
    public static Optional<LocalDateTime> firstOccurrenceFrom(Reminder reminder, LocalDateTime start) {
        boolean oneTime = reminder.getRepeat() == null || reminder.getRepeat() == RepeatPattern.NONE;
        if (oneTime || occursOn(reminder, start.toLocalDate())) {
            return Optional.of(start);
        }
        return nextOccurrenceAfter(reminder, start);
    }

    /** Does this reminder happen on this day? */
    private static boolean occursOn(Reminder reminder, LocalDate day) {
        if (reminder.getRepeat() == null) {
            return false; // old rows created before repeat existed behave like NONE
        }
        return switch (reminder.getRepeat()) {
            case NONE -> false;
            case DAILY -> true;
            case WEEKLY -> reminder.getDays().contains(day.getDayOfWeek());
        };
    }

    private static boolean isOnOrBeforeEndDate(Reminder reminder, LocalDateTime occurrence) {
        return reminder.getEndDate() == null || !occurrence.toLocalDate().isAfter(reminder.getEndDate());
    }
}
