package com.example.mamahelper.service;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.repository.ReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReminderNotificationSchedulerTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReminderNotificationScheduler reminderNotificationScheduler;

    @BeforeEach
    void setUp() {
        reminderRepository = mock(ReminderRepository.class);
        notificationService = mock(NotificationService.class);
        clock = getFixedClock(ZoneId.systemDefault(), Instant.parse("2026-10-29T10:00:00Z"));

        reminderNotificationScheduler = new ReminderNotificationScheduler(reminderRepository, clock, notificationService);
    }

    @Test
    void shouldSendNotificationWhenReminderIsTenMinutesAway() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(clock).plusMinutes(10));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(reminderRepository).save(reminder);
    }

    @Test
    void shouldNotSendNotificationWhenReminderIsMoreThanTenMinutesAway() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(clock).plusMinutes(15));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        assertFalse(reminder.isNotificationSent());
        verify(reminderRepository, never()).save(reminder);
    }

    @Test
    void shouldNotSendNotificationTwiceForTheSameReminder() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(clock).plusMinutes(10));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();
        reminderNotificationScheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(reminderRepository, times(1)).save(reminder);
    }

    @Test
    void shouldNotSendNotificationWhenReminderTimeAlreadyPassed() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = Instant.parse("2026-10-29T10:01:00Z");
        Clock fixedClock = getFixedClock(zoneId, fixedInstant);

        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(fixedClock).minusMinutes(1));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        assertFalse(reminder.isNotificationSent());
        verify(reminderRepository, never()).save(reminder);
    }

    @Test
    void shouldNotSendNotificationWhenReminderWasAlreadyNotified() {
        ZoneId zoneId = ZoneId.systemDefault();
        Instant fixedInstant = Instant.parse("2026-10-29T10:00:00Z");
        Clock fixedClock = getFixedClock(zoneId, fixedInstant);

        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(fixedClock).plusMinutes(10));
        reminder.setNotificationSent(true);

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(reminderRepository, never()).save(reminder);
    }

    @Test
    void shouldSendNotificationWhenUpdatedReminderIsDue() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now(clock).plusMinutes(30));
        reminder.setNotificationSent(false);

        reminder.setTime(LocalDateTime.now(clock).plusMinutes(10));

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();
        reminderNotificationScheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(reminderRepository, times(1)).save(reminder);
    }

    @Test
    void shouldSendNotificationWhenReminderIsDue() {
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setTitle("Doctor Appointment");
        reminder.setTime(LocalDateTime.now(clock).plusMinutes(10));
        reminder.setNotificationSent(false);

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(notificationService).createNotification(reminder);
    }

    @Test
    void shouldNotCreateDuplicatedNotification() {
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setTitle("Doctor Appointment");
        reminder.setTime(LocalDateTime.now(clock).plusMinutes(10));
        reminder.setNotificationSent(true);

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(notificationService, never()).createNotification(reminder);
    }

    @Test
    void shouldNotSendNotificationWhenReminderIsDueButNotificationAlreadySent() {
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setTitle("Doctor Appointment");
        reminder.setTime(LocalDateTime.now(clock).plusMinutes(10));
        reminder.setNotificationSent(true);

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        assertTrue(reminder.isNotificationSent());
        verify(notificationService, never()).createNotification(reminder);
    }

    @Test
    void shouldNotCreateNotificationWhenReminderIsNotDue() {
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setTitle("Doctor Appointment");
        reminder.setTime(LocalDateTime.now(clock).plusHours(2));
        reminder.setNotificationSent(false);

        when(reminderRepository.findAll()).thenReturn(List.of(reminder));

        reminderNotificationScheduler.checkReminders();

        verify(notificationService, never()).createNotification(reminder);
    }

    private Clock getFixedClock(ZoneId zoneId, Instant fixedInstant) {
        return Clock.fixed(fixedInstant, zoneId);
    }
}