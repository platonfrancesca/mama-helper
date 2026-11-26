package com.example.mamahelper.controller.dto;

import com.example.mamahelper.entity.Child;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ChildRequest(
        @NotBlank String name,
        @NotNull LocalDate dateOfBirth
) {
    public Child toEntity() {
        Child child = new Child();
        child.setName(name);
        child.setDateOfBirth(dateOfBirth);
        return child;
    }
}
