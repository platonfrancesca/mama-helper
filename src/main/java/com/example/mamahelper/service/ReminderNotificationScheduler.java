package com.example.mamahelper.service;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ReminderNotificationScheduler {

    public static final int MINUTES = 10;

    private final Set<String> notifiedReminders = new HashSet<>();

    private final ReminderRepository reminderRepository;

    public ReminderNotificationScheduler(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        List<Reminder> reminders = reminderRepository.findAll();
        Set<Long> existingReminderIds = new HashSet<>();

        for (Reminder reminder : reminders) {
            existingReminderIds.add(reminder.getId());
            LocalDateTime reminderTime = reminder.getTime();

            if (reminderTime != null && !reminder.isNotificationSent()) {
                LocalDateTime notificationTime = reminderTime.minusMinutes(MINUTES);

                if (reminderTime.isAfter(now) && !notificationTime.isAfter(now)) {
                    String notificationKey = reminder.getId() + "_" + notificationTime;

                    if (notifiedReminders.add(notificationKey)) {
                        sendNotification(reminder);
                    }
                }
            }

            removeReminder(existingReminderIds);
        }
    }

    private void removeReminder(Set<Long> existingReminderIds) {
        notifiedReminders.removeIf(key -> {
            String[] parts = key.split("_");
            Long reminderId = Long.parseLong(parts[0]);
            return !existingReminderIds.contains(reminderId);
        });
    }

    private void sendNotification(Reminder reminder) {
        System.out.println("NOTIFICATION: " + reminder.getTitle() + " is coming up in " + MINUTES + " minutes");

        reminder.setNotificationSent(true);
        reminderRepository.save(reminder);
    }
}