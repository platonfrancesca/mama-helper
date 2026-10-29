package com.example.mamahelper.service;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReminderNotificationScheduler {

    public static final int MINUTES = 10;

    private final ReminderRepository reminderRepository;
    private final Clock clock;

    public ReminderNotificationScheduler(ReminderRepository reminderRepository,
                                         Clock clock) {
        this.reminderRepository = reminderRepository;
        this.clock = clock;
    }

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now(clock);

        List<Reminder> reminders = reminderRepository.findAll();
        for (Reminder reminder : reminders) {
            LocalDateTime reminderTime = reminder.getTime();

            if (reminderTime != null && !reminder.isNotificationSent()) {
                LocalDateTime notificationTime = reminderTime.minusMinutes(MINUTES);

                if (reminderTime.isAfter(now) && !notificationTime.isAfter(now))
                    sendNotification(reminder);
            }
        }
    }

    private void sendNotification(Reminder reminder) {
        reminder.setNotificationSent(true);
        reminderRepository.save(reminder);

        System.out.println("NOTIFICATION: " + reminder.getTitle() + " is coming up in " + MINUTES + " minutes");
    }
}