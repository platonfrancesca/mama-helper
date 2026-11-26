package com.example.mamahelper.controller;

import com.example.mamahelper.controller.exceptions.ResourceNotFoundException;
import com.example.mamahelper.entity.Child;
import com.example.mamahelper.service.ChildService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChildController.class)
class ChildControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChildService childService;

    @Test
    void shouldCreateChildWhenRequestIsValid() throws Exception {
        when(childService.createChild(any(Child.class)))
                .thenReturn(new Child(1L, "Maria", LocalDate.of(2026, 1, 1)));

        mockMvc.perform(post("/api/child")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maria", "dateOfBirth": "2026-01-01"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Maria"))
                .andExpect(jsonPath("$.dateOfBirth").value("2026-01-01"));
    }

    @Test
    void shouldReturnAllFieldErrorsWhenChildIsInvalid() throws Exception {
        mockMvc.perform(post("/api/child")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("must not be blank"))
                .andExpect(jsonPath("$.dateOfBirth").value("must not be null"));

        verify(childService, never()).createChild(any());
    }

    @Test
    void shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/child")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(childService, never()).createChild(any());
    }

    @Test
    void shouldReturnAllChildren() throws Exception {
        when(childService.getAllChildren()).thenReturn(List.of(
                new Child(1L, "Maria", LocalDate.of(2026, 1, 1)),
                new Child(2L, "Ana", LocalDate.of(2024, 5, 5))));

        mockMvc.perform(get("/api/child"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Maria"))
                .andExpect(jsonPath("$[1].name").value("Ana"));
    }

    @Test
    void shouldReturnChildById() throws Exception {
        when(childService.getChildById(1L))
                .thenReturn(new Child(1L, "Maria", LocalDate.of(2026, 1, 1)));

        mockMvc.perform(get("/api/child/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria"));
    }

    @Test
    void shouldReturnNotFoundWhenChildDoesNotExist() throws Exception {
        when(childService.getChildById(999L))
                .thenThrow(new ResourceNotFoundException("Child not found with id: 999"));

        mockMvc.perform(get("/api/child/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Child not found with id: 999"));
    }

    @Test
    void shouldUpdateChildWhenRequestIsValid() throws Exception {
        when(childService.updateChildInfo(eq(1L), any(Child.class)))
                .thenReturn(new Child(1L, "Maria Elena", LocalDate.of(2026, 1, 2)));

        mockMvc.perform(put("/api/child/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Maria Elena", "dateOfBirth": "2026-01-02"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Elena"))
                .andExpect(jsonPath("$.dateOfBirth").value("2026-01-02"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingChild() throws Exception {
        when(childService.updateChildInfo(eq(999L), any(Child.class)))
                .thenThrow(new ResourceNotFoundException("Child not found with id: 999"));

        mockMvc.perform(put("/api/child/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ana", "dateOfBirth": "2025-05-05"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Child not found with id: 999"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithInvalidData() throws Exception {
        mockMvc.perform(put("/api/child/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "dateOfBirth": "2026-01-02"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("must not be blank"));

        verify(childService, never()).updateChildInfo(any(), any());
    }
}
