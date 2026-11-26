package com.example.mamahelper.controller;

import com.example.mamahelper.controller.exceptions.ResourceNotFoundException;
import com.example.mamahelper.entity.Notification;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void shouldReturnAllNotifications() throws Exception {
        when(notificationService.getAllNotifications()).thenReturn(List.of(
                notification(1L, "Reminder: Bath time is coming up!", false),
                notification(2L, "Reminder: Vitamins is coming up!", true)));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].message").value("Reminder: Bath time is coming up!"))
                .andExpect(jsonPath("$[1].read").value(true));
    }

    @Test
    void shouldReturnOnlyReminderIdAndTitleInsteadOfWholeReminder() throws Exception {
        Reminder reminder = new Reminder();
        reminder.setId(7L);
        reminder.setTitle("Bath time");

        Notification notification = notification(1L, "Reminder: Bath time is coming up!", false);
        notification.setReminder(reminder);

        when(notificationService.getById(1L)).thenReturn(notification);

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reminderId").value(7))
                .andExpect(jsonPath("$.reminderTitle").value("Bath time"))
                .andExpect(jsonPath("$.reminder").doesNotExist());
    }

    @Test
    void shouldReturnNotificationById() throws Exception {
        when(notificationService.getById(1L))
                .thenReturn(notification(1L, "Reminder: Bath time is coming up!", false));

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenNotificationDoesNotExist() throws Exception {
        when(notificationService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Notification not found with id 999"));

        mockMvc.perform(get("/api/notifications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Notification not found with id 999"));
    }

    @Test
    void shouldMarkNotificationAsRead() throws Exception {
        when(notificationService.markAsRead(1L))
                .thenReturn(notification(1L, "Reminder: Bath time is coming up!", true));

        mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenMarkingMissingNotificationAsRead() throws Exception {
        when(notificationService.markAsRead(999L))
                .thenThrow(new ResourceNotFoundException("Notification not found with id 999"));

        mockMvc.perform(patch("/api/notifications/999/read"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNoContentWhenNotificationIsDeleted() throws Exception {
        mockMvc.perform(delete("/api/notifications/1"))
                .andExpect(status().isNoContent());

        verify(notificationService).deleteNotificationById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingNotification() throws Exception {
        // deleteNotificationById returns void, so we use doThrow(...).when(...) instead of when(...).thenThrow(...)
        doThrow(new ResourceNotFoundException("Notification not found with id 999"))
                .when(notificationService).deleteNotificationById(999L);

        mockMvc.perform(delete("/api/notifications/999"))
                .andExpect(status().isNotFound());
    }

    private Notification notification(Long id, String message, boolean read) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setMessage(message);
        notification.setRead(read);
        return notification;
    }
}
