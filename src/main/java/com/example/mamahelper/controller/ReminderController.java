package com.example.mamahelper.controller;

import com.example.mamahelper.controller.dto.CreateReminderRequest;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public Reminder createReminder(@RequestBody CreateReminderRequest request) {
        Reminder reminder = new Reminder();
        reminder.setTitle(request.getTitle());
        reminder.setDescription(request.getDescription());
        reminder.setTime(LocalDateTime.parse(request.getTime()));

        return reminderService.createReminder(reminder, request.getChildId());
    }

    @GetMapping
    public List<Reminder> getAllReminders() {
        return reminderService.getAllReminders();
    }

    @GetMapping("/{reminderId}")
    public ResponseEntity<Reminder> getReminderById(@PathVariable Long reminderId) {
        return reminderService.getReminderById(reminderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{reminderId}")
    public ResponseEntity<Reminder> updateReminder(@PathVariable Long reminderId,
                                                   @Valid @RequestBody Reminder updatedReminder) {
        return reminderService.updateReminder(reminderId, updatedReminder)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long reminderId) {
        return reminderService.deleteReminder(reminderId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
