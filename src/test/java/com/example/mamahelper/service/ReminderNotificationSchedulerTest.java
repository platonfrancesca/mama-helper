package com.example.mamahelper.service;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;
import com.example.mamahelper.repository.ReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Which reminders are due is decided by the database query (see ReminderRepositoryTest).
 * These tests check what the scheduler does with the reminders the query returns.
 */
class ReminderNotificationSchedulerTest {

    private ReminderRepository reminderRepository;
    private NotificationService notificationService;
    private Clock clock;
    private LocalDateTime now;

    private ReminderNotificationScheduler reminderNotificationScheduler;

    @BeforeEach
    void setUp() {
        reminderRepository = mock(ReminderRepository.class);
        notificationService = mock(NotificationService.class);
        clock = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneId.systemDefault());
        now = LocalDateTime.now(clock);

        reminderNotificationScheduler = new ReminderNotificationScheduler(reminderRepository, clock, notificationService);
    }

    @Test
    void shouldQueryRemindersDueWithinTheNextTenMinutes() {
        when(reminderRepository.findByNotificationSentFalseAndTimeAfterAndTimeLessThanEqual(any(), any()))
                .thenReturn(List.of());

        reminderNotificationScheduler.checkReminders();

        verify(reminderRepository).findByNotificationSentFalseAndTimeAfterAndTimeLessThanEqual(now, now.plusMinutes(10));
        verify(reminderRepository, never()).findAll();
    }

    @Test
    void shouldMarkDueReminderAsSentAndSaveIt() {
        Reminder reminder = reminder(1L, "Bath Time", now.plusMinutes(10));
        whenDueRemindersAre(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(reminderRepository).save(reminder);
    }

    @Test
    void shouldCreateNotificationForDueReminder() {
        Reminder reminder = reminder(1L, "Doctor Appointment", now.plusMinutes(5));
        whenDueRemindersAre(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        verify(notificationService).createNotification(reminder);
    }

    @Test
    void shouldNotifyEveryDueReminder() {
        Reminder bath = reminder(1L, "Bath Time", now.plusMinutes(3));
        Reminder vitamins = reminder(2L, "Vitamins", now.plusMinutes(8));
        whenDueRemindersAre(List.of(bath, vitamins));

        reminderNotificationScheduler.checkReminders();

        verify(notificationService).createNotification(bath);
        verify(notificationService).createNotification(vitamins);
    }

    @Test
    void shouldDoNothingWhenNoRemindersAreDue() {
        whenDueRemindersAre(List.of());

        reminderNotificationScheduler.checkReminders();

        verify(reminderRepository, never()).save(any());
        verify(notificationService, never()).createNotification(any());
    }

    @Test
    void shouldNotSendNotificationTwiceForTheSameReminder() {
        Reminder reminder = reminder(1L, "Bath Time", now.plusMinutes(10));
        // First run: the reminder is due. Second run: the database no longer returns it (notificationSent = true)
        when(reminderRepository.findByNotificationSentFalseAndTimeAfterAndTimeLessThanEqual(any(), any()))
                .thenReturn(List.of(reminder))
                .thenReturn(List.of());

        reminderNotificationScheduler.checkReminders();
        reminderNotificationScheduler.checkReminders();

        verify(reminderRepository, times(1)).save(reminder);
        verify(notificationService, times(1)).createNotification(reminder);
    }

    @Test
    void shouldMoveDailyReminderToTomorrowAfterNotifying() {
        LocalDateTime dueTime = now.plusMinutes(10);
        Reminder vitamins = reminder(1L, "Vitamins", dueTime);
        vitamins.setRepeat(RepeatPattern.DAILY);
        whenDueRemindersAre(List.of(vitamins));

        reminderNotificationScheduler.checkReminders();

        verify(notificationService).createNotification(vitamins);
        assertEquals(dueTime.plusDays(1), vitamins.getTime());
        assertFalse(vitamins.isNotificationSent());
        verify(reminderRepository).save(vitamins);
    }

    @Test
    void shouldFinishRepeatingReminderAfterItsLastOccurrence() {
        LocalDateTime dueTime = now.plusMinutes(10);
        Reminder medicine = reminder(1L, "Medicine", dueTime);
        medicine.setRepeat(RepeatPattern.DAILY);
        medicine.setEndDate(dueTime.toLocalDate());
        whenDueRemindersAre(List.of(medicine));

        reminderNotificationScheduler.checkReminders();

        assertEquals(dueTime, medicine.getTime());
        assertTrue(medicine.isNotificationSent());
    }

    @Test
    void shouldSkipMissedOccurrenceWithoutNotifying() {
        // The server was down: the 8:00 occurrence passed two hours ago
        LocalDateTime missedTime = now.minusHours(2);
        Reminder vitamins = reminder(1L, "Vitamins", missedTime);
        vitamins.setRepeat(RepeatPattern.DAILY);
        when(reminderRepository.findByNotificationSentFalseAndRepeatNotAndTimeLessThanEqual(RepeatPattern.NONE, now))
                .thenReturn(List.of(vitamins));
        whenDueRemindersAre(List.of());

        reminderNotificationScheduler.checkReminders();

        verify(notificationService, never()).createNotification(any());
        assertEquals(missedTime.plusDays(1), vitamins.getTime());
        assertFalse(vitamins.isNotificationSent());
        verify(reminderRepository).save(vitamins);
    }

    @Test
    void shouldFinishMissedReminderWhenItHasNoNextOccurrence() {
        LocalDateTime missedTime = now.minusHours(2);
        Reminder medicine = reminder(1L, "Medicine", missedTime);
        medicine.setRepeat(RepeatPattern.DAILY);
        medicine.setEndDate(missedTime.toLocalDate());
        when(reminderRepository.findByNotificationSentFalseAndRepeatNotAndTimeLessThanEqual(RepeatPattern.NONE, now))
                .thenReturn(List.of(medicine));
        whenDueRemindersAre(List.of());

        reminderNotificationScheduler.checkReminders();

        verify(notificationService, never()).createNotification(any());
        assertTrue(medicine.isNotificationSent());
    }

    private void whenDueRemindersAre(List<Reminder> reminders) {
        when(reminderRepository.findByNotificationSentFalseAndTimeAfterAndTimeLessThanEqual(any(), any()))
                .thenReturn(reminders);
    }

    private Reminder reminder(Long id, String title, LocalDateTime time) {
        Reminder reminder = new Reminder();
        reminder.setId(id);
        reminder.setTitle(title);
        reminder.setTime(time);
        return reminder;
    }
}
