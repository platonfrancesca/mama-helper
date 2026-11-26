package com.example.mamahelper.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateReminderRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 200)
    private String description;

    @NotNull
    private LocalDateTime time;

    @NotNull
    private Long childId;
}
