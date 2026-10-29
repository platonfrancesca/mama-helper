package com.example.mamahelper.service;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.repository.ReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

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
    void shouldReturnEmptyWhenReminderDoesNotExistById() {
        when(reminderRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<Reminder> result = reminderService.updateReminder(1L, new Reminder());

        assertTrue(result.isEmpty());
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
}