package com.example.mamahelper.service;

import com.example.mamahelper.controller.exceptions.ResourceNotFoundException;
import com.example.mamahelper.entity.Notification;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    void shouldCreateNotification() {
        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification notification = notificationService.createNotification(reminder);

        assertNotNull(notification);
        assertEquals("Reminder: Bath Time is coming up!", notification.getMessage());
        assertSame(reminder, notification.getReminder());

        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void shouldReturnAllNotifications() {
        Notification notification1 = new Notification();
        notification1.setMessage("Bath Time");

        Notification notification2 = new Notification();
        notification2.setMessage("Give vitamins");

        List<Notification> notifications = List.of(notification1, notification2);
        when(notificationRepository.findAll()).thenReturn(notifications);

        List<Notification> results = notificationService.getAllNotifications();

        assertEquals(2, results.size());
        assertTrue(results.contains(notification1));
        assertEquals("Bath Time", results.get(0).getMessage());
        assertTrue(results.contains(notification2));
        assertEquals("Give vitamins", results.get(1).getMessage());

        verify(notificationRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnNotificationById() {
        Notification notification = new Notification();
        var testId = 1L;
        notification.setId(testId);

        when(notificationRepository.findById(testId))
                .thenReturn(Optional.of(notification));

        Notification result = notificationService.getById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());

        verify(notificationRepository).findById(testId);
    }

    @Test
    void shouldThrowExceptionWhenNotificationNotFoundById() {
        Long testId = 999L;

        when(notificationRepository.findById(testId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notificationService.getById(testId));

        verify(notificationRepository).findById(testId);
    }

    @Test
    void shouldMarkNotificationAsRead() {
        Notification notification = new Notification();
        notification.setMessage("Bath Time");
        notification.setRead(false);

        Long notificationId = 1L;

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.markAsRead(notificationId);

        assertTrue(result.isRead());

        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void shouldThrowExceptionWhenNotificationNotFound() {
        Long notificationId = 987L;

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> notificationService.markAsRead(notificationId));

        assertEquals("Notification not found with id " + notificationId, exception.getMessage());

        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void shouldDeleteNotificationById() {
        Long notificationId = 1L;
        Notification notification = new Notification();
        notification.setId(notificationId);

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        notificationService.deleteNotificationById(notificationId);

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).delete(notification);
    }
}