package com.example.mamahelper.controller;

import com.example.mamahelper.controller.exceptions.ResourceNotFoundException;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.service.ReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReminderController.class)
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReminderService reminderService;

    @Test
    void shouldCreateReminderWhenRequestIsValid() throws Exception {
        Reminder savedReminder = new Reminder();
        savedReminder.setId(1L);
        savedReminder.setTitle("Bath time");
        savedReminder.setTime(LocalDateTime.of(2026, 12, 1, 19, 0));

        when(reminderService.createReminder(any(Reminder.class), eq(1L)))
                .thenReturn(savedReminder);

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Bath time", "time": "2026-12-01T19:00:00", "childId": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Bath time"))
                .andExpect(jsonPath("$.time").value("2026-12-01T19:00:00"));
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "time": "2026-12-01T19:00:00", "childId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("must not be blank"));

        verify(reminderService, never()).createReminder(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenChildIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Bath time", "time": "2026-12-01T19:00:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.childId").value("must not be null"));

        verify(reminderService, never()).createReminder(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenTimeHasInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Bath time", "time": "tomorrow", "childId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(reminderService, never()).createReminder(any(), any());
    }

    @Test
    void shouldReturnNotFoundWhenChildDoesNotExist() throws Exception {
        when(reminderService.createReminder(any(Reminder.class), eq(999L)))
                .thenThrow(new ResourceNotFoundException("Child not found with id: 999"));

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Bath time", "time": "2026-12-01T19:00:00", "childId": 999}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Child not found with id: 999"));
    }

    @Test
    void shouldReturnNotFoundWhenReminderDoesNotExist() throws Exception {
        when(reminderService.getReminderById(999L))
                .thenThrow(new ResourceNotFoundException("Reminder not found with id: 999"));

        mockMvc.perform(get("/api/reminders/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Reminder not found with id: 999"));
    }

    @Test
    void shouldReturnNoContentWhenReminderIsDeleted() throws Exception {
        when(reminderService.deleteReminder(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/reminders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingReminder() throws Exception {
        when(reminderService.deleteReminder(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/reminders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenDatabaseConstraintIsViolated() throws Exception {
        when(reminderService.deleteReminder(anyLong()))
                .thenThrow(new DataIntegrityViolationException("FK constraint violated"));

        mockMvc.perform(delete("/api/reminders/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("The request conflicts with existing data."));
    }
}
