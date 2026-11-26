package com.example.mamahelper.service;

import com.example.mamahelper.controller.exceptions.ResourceNotFoundException;
import com.example.mamahelper.entity.Child;
import com.example.mamahelper.repository.ChildRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ChildServiceTest {

    @Mock
    private ChildRepository childRepository;

    @InjectMocks
    private ChildService childService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    void shouldReturnChildById() {
        Child child = new Child(1L, "Maria", LocalDate.of(2026, 1, 1));
        when(childRepository.findById(1L)).thenReturn(Optional.of(child));

        Child result = childService.getChildById(1L);

        assertSame(child, result);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenGettingMissingChild() {
        when(childRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> childService.getChildById(999L));

        assertEquals("Child not found with id: 999", exception.getMessage());
    }

    @Test
    void shouldUpdateChild() {
        Long childId = 1L;
        Child existingChild = new Child(childId, "Maria", LocalDate.of(2026, 1, 1));
        Child updatedChildInfo = new Child(null, "Maria Elena", LocalDate.of(2026, 1, 2));

        when(childRepository.findById(childId))
                .thenReturn(Optional.of(existingChild));
        when(childRepository.save(any(Child.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Child result = childService.updateChildInfo(childId, updatedChildInfo);

        assertEquals(childId, result.getId());
        assertEquals("Maria Elena", result.getName());
        assertEquals(LocalDate.of(2026, 1, 2), result.getDateOfBirth());

        verify(childRepository).findById(childId);
        verify(childRepository).save(existingChild);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenChildDoesNotExist() {
        Long childId = 999L;
        Child updatedChildInfo = new Child(null, "Ana", LocalDate.of(2025, 5, 5));

        when(childRepository.findById(childId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> childService.updateChildInfo(childId, updatedChildInfo));

        assertEquals("Child not found with id: " + childId, exception.getMessage());

        verify(childRepository).findById(childId);
        verify(childRepository, never()).save(any(Child.class));
    }
}
