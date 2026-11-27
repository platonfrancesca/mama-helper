package com.example.mamahelper.service;

import com.example.mamahelper.controller.exceptions.ResourceNotFoundException;
import com.example.mamahelper.entity.Child;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;
import com.example.mamahelper.repository.ChildRepository;
import com.example.mamahelper.repository.NotificationRepository;
import com.example.mamahelper.repository.ReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ChildRepository childRepository;

    @InjectMocks
    private ReminderService reminderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldResetNotificationWhenReminderTimeChanges() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setNotificationSent(true);

        LocalDateTime oldTime = LocalDateTime.of(2026, 10, 29, 14, 0);
        LocalDateTime newTime = LocalDateTime.of(2026, 10, 29, 15, 0);

        reminder.setTime(oldTime);

        when(reminderRepository.findById(1L))
                .thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reminder updatedReminder = new Reminder();
        updatedReminder.setTitle("Bath Time");
        updatedReminder.setDescription("Updated description");
        updatedReminder.setTime(newTime);

        reminderService.updateReminder(1L, updatedReminder);

        assertFalse(reminder.isNotificationSent());
    }

    @Test
    void shouldNotResetNotificationWhenOnlyTitleChanges() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setNotificationSent(true);

        LocalDateTime oldTime = LocalDateTime.of(2026, 10, 29, 14, 0);
        reminder.setTime(oldTime);

        when(reminderRepository.findById(1L))
                .thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reminder updatedReminder = new Reminder();
        updatedReminder.setTitle("Evening Bath");
        updatedReminder.setTime(oldTime);

        reminderService.updateReminder(1L, updatedReminder);

        assertTrue(reminder.isNotificationSent());
    }

    @Test
    void shouldThrowExceptionWhenReminderDoesNotExistById() {
        var reminderId = 1L;
        when(reminderRepository.findById(reminderId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reminderService.updateReminder(reminderId, new Reminder()));

        assertEquals("Reminder not found with id: " + reminderId, exception.getMessage());

        verify(reminderRepository, times(1)).findById(reminderId);
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void shouldNotChangeNotificationSentIfNewTimeIsEqualToOldTime() {
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setTitle("Bath Time");
        reminder.setNotificationSent(true);

        LocalDateTime reminderTime = LocalDateTime.of(2026, 10, 29, 20, 0);
        reminder.setTime(reminderTime);

        Reminder updatedReminder = new Reminder();
        updatedReminder.setTitle("Evening Bath");
        updatedReminder.setTime(reminderTime);

        when(reminderRepository.findById(1L))
                .thenReturn(Optional.of(reminder));
        when(reminderRepository.save(any(Reminder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        reminderService.updateReminder(1L, updatedReminder);

        assertTrue(reminder.isNotificationSent());
    }

    @Test
    void shouldMoveNewWeeklyReminderToItsFirstChosenDay() {
        // 2026-12-01 is a Tuesday; the bath is on Thursdays
        Reminder bath = new Reminder();
        bath.setTitle("Bath time");
        bath.setTime(LocalDateTime.of(2026, 12, 1, 19, 0));
        bath.setRepeat(RepeatPattern.WEEKLY);
        bath.setDays(new HashSet<>(Set.of(DayOfWeek.THURSDAY)));

        when(childRepository.findById(1L))
                .thenReturn(Optional.of(new Child(1L, "Maria", LocalDate.of(2026, 1, 1))));
        when(reminderRepository.save(any(Reminder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reminder saved = reminderService.createReminder(bath, 1L);

        assertEquals(LocalDateTime.of(2026, 12, 3, 19, 0), saved.getTime());
    }

    @Test
    void shouldKeepTimeOfNewOneTimeReminder() {
        LocalDateTime doctorTime = LocalDateTime.of(2026, 12, 1, 9, 30);
        Reminder doctor = new Reminder();
        doctor.setTitle("Doctor");
        doctor.setTime(doctorTime);

        when(childRepository.findById(1L))
                .thenReturn(Optional.of(new Child(1L, "Maria", LocalDate.of(2026, 1, 1))));
        when(reminderRepository.save(any(Reminder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(doctorTime, reminderService.createReminder(doctor, 1L).getTime());
    }

    @Test
    void shouldApplyNewRepeatPatternAndResetNotificationWhenRepeatChanges() {
        LocalDateTime time = LocalDateTime.of(2026, 12, 1, 8, 0);
        Reminder vitamins = new Reminder();
        vitamins.setTitle("Vitamins");
        vitamins.setTime(time);
        vitamins.setNotificationSent(true);

        when(reminderRepository.findById(1L)).thenReturn(Optional.of(vitamins));
        when(reminderRepository.save(any(Reminder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reminder updated = new Reminder();
        updated.setTitle("Vitamins");
        updated.setTime(time);
        updated.setRepeat(RepeatPattern.DAILY);
        updated.setEndDate(LocalDate.of(2027, 3, 31));

        reminderService.updateReminder(1L, updated);

        assertEquals(RepeatPattern.DAILY, vitamins.getRepeat());
        assertEquals(LocalDate.of(2027, 3, 31), vitamins.getEndDate());
        assertFalse(vitamins.isNotificationSent());
    }

    @Test
    void shouldDeleteNotificationsBeforeDeletingReminder() {
        Long reminderId = 1L;
        when(reminderRepository.existsById(reminderId)).thenReturn(true);

        reminderService.deleteReminder(reminderId);

        InOrder inOrder = inOrder(notificationRepository, reminderRepository);
        inOrder.verify(notificationRepository).deleteByReminderId(reminderId);
        inOrder.verify(reminderRepository).deleteById(reminderId);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionAndDeleteNothingWhenReminderDoesNotExist() {
        Long reminderId = 999L;
        when(reminderRepository.existsById(reminderId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reminderService.deleteReminder(reminderId));

        verify(notificationRepository, never()).deleteByReminderId(any());
        verify(reminderRepository, never()).deleteById(any());
    }
}