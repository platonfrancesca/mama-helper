package com.example.mamahelper.controller;

import com.example.mamahelper.controller.dto.CreateReminderRequest;
import com.example.mamahelper.controller.dto.ReminderResponse;
import com.example.mamahelper.controller.dto.UpdateReminderRequest;
import com.example.mamahelper.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ReminderResponse createReminder(@Valid @RequestBody CreateReminderRequest request) {
        return ReminderResponse.from(reminderService.createReminder(request.toEntity(), request.getChildId()));
    }

    @GetMapping
    public List<ReminderResponse> getAllReminders() {
        return reminderService.getAllReminders().stream()
                .map(ReminderResponse::from)
                .toList();
    }

    @GetMapping("/{reminderId}")
    public ResponseEntity<ReminderResponse> getReminderById(@PathVariable Long reminderId) {
        return ResponseEntity.ok(ReminderResponse.from(reminderService.getReminderById(reminderId)));
    }

    @PutMapping("/{reminderId}")
    public ResponseEntity<ReminderResponse> updateReminder(@PathVariable Long reminderId,
                                                           @Valid @RequestBody UpdateReminderRequest request) {
        return ResponseEntity.ok(ReminderResponse.from(reminderService.updateReminder(reminderId, request.toEntity())));
    }

    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long reminderId) {
        reminderService.deleteReminder(reminderId);

        return ResponseEntity.noContent().build();
    }
}
