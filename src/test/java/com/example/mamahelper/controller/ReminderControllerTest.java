package com.example.mamahelper.controller;

import com.example.mamahelper.controller.exceptions.ResourceNotFoundException;
import com.example.mamahelper.entity.Child;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;
import com.example.mamahelper.service.ReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
        savedReminder.setChild(new Child(1L, "Maria", LocalDate.of(2026, 1, 1)));

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
                .andExpect(jsonPath("$.time").value("2026-12-01T19:00:00"))
                .andExpect(jsonPath("$.notificationSent").value(false))
                // the mobile app reads reminder.child.name, so the nested child must stay
                .andExpect(jsonPath("$.child.id").value(1))
                .andExpect(jsonPath("$.child.name").value("Maria"));
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "time": "2026-12-01T19:00:00", "childId": 1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("must not be blank"));

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
                .andExpect(jsonPath("$.errors.childId").value("must not be null"));

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
                .andExpect(jsonPath("$.title").value("Malformed request"));

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
                .andExpect(jsonPath("$.detail").value("Child not found with id: 999"));
    }

    @Test
    void shouldReturnNotFoundWhenReminderDoesNotExist() throws Exception {
        when(reminderService.getReminderById(999L))
                .thenThrow(new ResourceNotFoundException("Reminder not found with id: 999"));

        mockMvc.perform(get("/api/reminders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Reminder not found with id: 999"));
    }

    @Test
    void shouldReturnNoContentWhenReminderIsDeleted() throws Exception {
        mockMvc.perform(delete("/api/reminders/1"))
                .andExpect(status().isNoContent());

        verify(reminderService).deleteReminder(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingReminder() throws Exception {
        doThrow(new ResourceNotFoundException("Reminder not found with id: 999"))
                .when(reminderService).deleteReminder(999L);

        mockMvc.perform(delete("/api/reminders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Reminder not found with id: 999"));
    }

    @Test
    void shouldReturnConflictWhenDatabaseConstraintIsViolated() throws Exception {
        doThrow(new DataIntegrityViolationException("FK constraint violated"))
                .when(reminderService).deleteReminder(anyLong());

        mockMvc.perform(delete("/api/reminders/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("The request conflicts with existing data."));
    }

    @Test
    void shouldUpdateReminderWhenRequestIsValid() throws Exception {
        Reminder updatedReminder = new Reminder();
        updatedReminder.setId(1L);
        updatedReminder.setTitle("Evening bath");
        updatedReminder.setTime(LocalDateTime.of(2026, 12, 1, 20, 0));
        updatedReminder.setChild(new Child(1L, "Maria", LocalDate.of(2026, 1, 1)));

        when(reminderService.updateReminder(eq(1L), any(Reminder.class)))
                .thenReturn(updatedReminder);

        mockMvc.perform(put("/api/reminders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Evening bath", "time": "2026-12-01T20:00:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Evening bath"))
                .andExpect(jsonPath("$.time").value("2026-12-01T20:00:00"))
                .andExpect(jsonPath("$.child.name").value("Maria"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingReminderWithInvalidData() throws Exception {
        mockMvc.perform(put("/api/reminders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("must not be blank"))
                .andExpect(jsonPath("$.errors.time").value("must not be null"));

        verify(reminderService, never()).updateReminder(any(), any());
    }

    @Test
    void shouldReturnProblemDetailWithAllFieldsForValidationErrors() throws Exception {
        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.instance").value("/api/reminders"))
                .andExpect(jsonPath("$.errors.title").value("must not be blank"))
                .andExpect(jsonPath("$.errors.time").value("must not be null"))
                .andExpect(jsonPath("$.errors.childId").value("must not be null"));
    }

    @Test
    void shouldNotLeakInternalDetailsOnUnexpectedError() throws Exception {
        when(reminderService.getAllReminders())
                .thenThrow(new RuntimeException("secret SQL: select * from reminder"));

        mockMvc.perform(get("/api/reminders"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret"))));
    }

    @Test
    void shouldReturnNotFoundForUnknownUrl() throws Exception {
        mockMvc.perform(get("/api/reminders/1/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnMethodNotAllowedForWrongHttpMethod() throws Exception {
        mockMvc.perform(patch("/api/reminders"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void shouldReturnRepeatFieldsForWeeklyReminder() throws Exception {
        Reminder savedReminder = new Reminder();
        savedReminder.setId(2L);
        savedReminder.setTitle("Bath time");
        savedReminder.setTime(LocalDateTime.of(2026, 12, 3, 19, 0));
        savedReminder.setRepeat(RepeatPattern.WEEKLY);
        savedReminder.setDays(new HashSet<>(Set.of(DayOfWeek.THURSDAY, DayOfWeek.MONDAY)));
        savedReminder.setEndDate(LocalDate.of(2027, 3, 31));
        savedReminder.setChild(new Child(1L, "Maria", LocalDate.of(2026, 1, 1)));

        when(reminderService.createReminder(any(Reminder.class), eq(1L)))
                .thenReturn(savedReminder);

        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Bath time", "time": "2026-12-01T19:00:00", "childId": 1,
                                 "repeat": "WEEKLY", "days": ["MONDAY", "THURSDAY"], "endDate": "2027-03-31"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repeat").value("WEEKLY"))
                // sorted Monday to Sunday
                .andExpect(jsonPath("$.days[0]").value("MONDAY"))
                .andExpect(jsonPath("$.days[1]").value("THURSDAY"))
                .andExpect(jsonPath("$.endDate").value("2027-03-31"))
                .andExpect(jsonPath("$.time").value("2026-12-03T19:00:00"));
    }

    @Test
    void shouldReturnNoneAndEmptyDaysForOneTimeReminder() throws Exception {
        Reminder savedReminder = new Reminder();
        savedReminder.setId(1L);
        savedReminder.setTitle("Doctor");
        savedReminder.setTime(LocalDateTime.of(2026, 12, 1, 9, 0));

        when(reminderService.getReminderById(1L)).thenReturn(savedReminder);

        mockMvc.perform(get("/api/reminders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repeat").value("NONE"))
                .andExpect(jsonPath("$.days").isEmpty())
                .andExpect(jsonPath("$.endDate").doesNotExist());
    }

    @Test
    void shouldReturnBadRequestWhenWeeklyReminderHasNoDays() throws Exception {
        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Bath time", "time": "2026-12-01T19:00:00", "childId": 1, "repeat": "WEEKLY"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.daysMatchRepeat").exists());

        verify(reminderService, never()).createReminder(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenDailyReminderHasDays() throws Exception {
        mockMvc.perform(put("/api/reminders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Vitamins", "time": "2026-12-01T08:00:00", "repeat": "DAILY", "days": ["MONDAY"]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.daysMatchRepeat").exists());

        verify(reminderService, never()).updateReminder(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenEndDateIsBeforeTime() throws Exception {
        mockMvc.perform(post("/api/reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Medicine", "time": "2026-12-01T08:00:00", "childId": 1,
                                 "repeat": "DAILY", "endDate": "2026-11-30"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.endDateValid").value("must not be before the first occurrence"));

        verify(reminderService, never()).createReminder(any(), any());
    }
}
