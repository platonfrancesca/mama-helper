package com.example.mamahelper.controller.dto;

import com.example.mamahelper.entity.Child;

import java.time.LocalDate;

public record ChildResponse(Long id, String name, LocalDate dateOfBirth) {

    public static ChildResponse from(Child child) {
        return new ChildResponse(child.getId(), child.getName(), child.getDateOfBirth());
    }
}
