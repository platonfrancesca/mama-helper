package com.example.mamahelper.service;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.repository.ReminderRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReminderNotificationSchedulerTest {

    @Test
    void shouldSendNotificationWhenReminderIsTenMinutesAway() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = Instant.parse("2026-10-29T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        ReminderRepository reminderRepository = mock(ReminderRepository.class);
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(fixedClock).plusMinutes(10));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        ReminderNotificationScheduler scheduler = new ReminderNotificationScheduler(reminderRepository, fixedClock);

        scheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(reminderRepository).save(reminder);
    }

    @Test
    void shouldNotSendNotificationWhenReminderIsMoreThanTenMinutesAway() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = Instant.parse("2026-10-29T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        ReminderRepository reminderRepository = mock(ReminderRepository.class);
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(fixedClock).plusMinutes(15));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        ReminderNotificationScheduler scheduler = new ReminderNotificationScheduler(reminderRepository, fixedClock);

        scheduler.checkReminders();

        assertFalse(reminder.isNotificationSent());
        verify(reminderRepository, never()).save(reminder);
    }

    @Test
    void shouldNotSendNotificationTwiceForTheSameReminder() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = Instant.parse("2026-10-29T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        ReminderRepository reminderRepository = mock(ReminderRepository.class);
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(fixedClock).plusMinutes(10));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        ReminderNotificationScheduler scheduler = new ReminderNotificationScheduler(reminderRepository, fixedClock);

        scheduler.checkReminders();
        scheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(reminderRepository, times(1)).save(reminder);
    }

    @Test
    void shouldNotSendNotificationWhenReminderTimeAlreadyPassed() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = Instant.parse("2026-10-29T10:01:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        ReminderRepository reminderRepository = mock(ReminderRepository.class);
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(fixedClock).minusMinutes(1));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        ReminderNotificationScheduler scheduler = new ReminderNotificationScheduler(reminderRepository, fixedClock);

        scheduler.checkReminders();

        assertFalse(reminder.isNotificationSent());
        verify(reminderRepository, never()).save(reminder);
    }

    @Test
    void shouldNotSendNotificationWhenReminderWasAlreadyNotified() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = Instant.parse("2026-10-29T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        ReminderRepository reminderRepository = mock(ReminderRepository.class);
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(fixedClock).plusMinutes(10));
        reminder.setNotificationSent(true);

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        ReminderNotificationScheduler scheduler = new ReminderNotificationScheduler(reminderRepository, fixedClock);

        scheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(reminderRepository, never()).save(reminder);
    }

    @Test
    void shouldSendNotificationWhenUpdatedReminderIsDue() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = Instant.parse("2026-10-29T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, zoneId);

        ReminderRepository reminderRepository = mock(ReminderRepository.class);
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(fixedClock).plusMinutes(30));
        reminder.setNotificationSent(false);

        reminder.setTime(LocalDateTime.now(fixedClock).plusMinutes(10));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        ReminderNotificationScheduler scheduler = new ReminderNotificationScheduler(reminderRepository, fixedClock);

        scheduler.checkReminders();
        scheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(reminderRepository, times(1)).save(reminder);
    }
}