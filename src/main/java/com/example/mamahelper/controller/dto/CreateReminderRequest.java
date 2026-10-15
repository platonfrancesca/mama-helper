package com.example.mamahelper.controller.dto;

import lombok.Data;

@Data
public class CreateReminderRequest {

    private String title;
    private String description;
    private String time;
    private Long childId;
}
