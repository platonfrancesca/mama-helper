package com.example.mamahelper.service;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NextOccurrenceCalculatorTest {

    // 2026-12-01 is a Tuesday
    private static final LocalDateTime TUESDAY_8AM = LocalDateTime.of(2026, 12, 1, 8, 0);

    @Test
    void dailyReminderComesBackTomorrowAtTheSameTime() {
        Reminder vitamins = reminder(RepeatPattern.DAILY, TUESDAY_8AM);

        assertEquals(Optional.of(TUESDAY_8AM.plusDays(1)),
                NextOccurrenceCalculator.nextOccurrenceAfter(vitamins, TUESDAY_8AM));
    }

    @Test
    void dailyReminderStillComesTodayWhenItsTimeHasNotPassed() {
        Reminder vitamins = reminder(RepeatPattern.DAILY, TUESDAY_8AM);
        LocalDateTime earlyMorning = TUESDAY_8AM.withHour(6);

        assertEquals(Optional.of(TUESDAY_8AM),
                NextOccurrenceCalculator.nextOccurrenceAfter(vitamins, earlyMorning));
    }

    @Test
    void weeklyReminderOnTuesdayGoesToThursday() {
        Reminder bath = reminder(RepeatPattern.WEEKLY, TUESDAY_8AM.withHour(19));
        bath.setDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY));

        assertEquals(Optional.of(LocalDateTime.of(2026, 12, 3, 19, 0)),
                NextOccurrenceCalculator.nextOccurrenceAfter(bath, TUESDAY_8AM));
    }

    @Test
    void weeklyReminderOnThursdayAfterItsTimeGoesToNextMonday() {
        Reminder bath = reminder(RepeatPattern.WEEKLY, TUESDAY_8AM.withHour(19));
        bath.setDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY));
        LocalDateTime thursdayEvening = LocalDateTime.of(2026, 12, 3, 19, 0);

        assertEquals(Optional.of(LocalDateTime.of(2026, 12, 7, 19, 0)),
                NextOccurrenceCalculator.nextOccurrenceAfter(bath, thursdayEvening));
    }

    @Test
    void noNextOccurrencePastTheEndDate() {
        Reminder medicine = reminder(RepeatPattern.DAILY, TUESDAY_8AM);
        medicine.setEndDate(LocalDate.of(2026, 12, 1));

        assertEquals(Optional.empty(), NextOccurrenceCalculator.nextOccurrenceAfter(medicine, TUESDAY_8AM));
    }

    @Test
    void occurrenceOnTheEndDateItselfStillCounts() {
        Reminder medicine = reminder(RepeatPattern.DAILY, TUESDAY_8AM);
        medicine.setEndDate(LocalDate.of(2026, 12, 2));

        assertEquals(Optional.of(TUESDAY_8AM.plusDays(1)),
                NextOccurrenceCalculator.nextOccurrenceAfter(medicine, TUESDAY_8AM));
    }

    @Test
    void oneTimeReminderHasNoNextOccurrence() {
        Reminder doctor = reminder(RepeatPattern.NONE, TUESDAY_8AM);

        assertEquals(Optional.empty(), NextOccurrenceCalculator.nextOccurrenceAfter(doctor, TUESDAY_8AM));
    }

    @Test
    void firstOccurrenceOfWeeklyReminderMovesToAChosenDay() {
        Reminder bath = reminder(RepeatPattern.WEEKLY, TUESDAY_8AM);
        bath.setDays(Set.of(DayOfWeek.FRIDAY));

        assertEquals(Optional.of(LocalDateTime.of(2026, 12, 4, 8, 0)),
                NextOccurrenceCalculator.firstOccurrenceFrom(bath, TUESDAY_8AM));
    }

    @Test
    void firstOccurrenceStaysWhenItIsAlreadyOnAChosenDay() {
        Reminder bath = reminder(RepeatPattern.WEEKLY, TUESDAY_8AM);
        bath.setDays(Set.of(DayOfWeek.TUESDAY));

        assertEquals(Optional.of(TUESDAY_8AM), NextOccurrenceCalculator.firstOccurrenceFrom(bath, TUESDAY_8AM));
    }

    private Reminder reminder(RepeatPattern repeat, LocalDateTime time) {
        Reminder reminder = new Reminder();
        reminder.setTitle("Test");
        reminder.setRepeat(repeat);
        reminder.setTime(time);
        return reminder;
    }
}
