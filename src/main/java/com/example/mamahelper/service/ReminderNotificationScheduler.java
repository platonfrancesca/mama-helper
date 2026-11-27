package com.example.mamahelper.service;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;
import com.example.mamahelper.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ReminderNotificationScheduler {

    public static final int MINUTES = 10;

    private final ReminderRepository reminderRepository;
    private final Clock clock;
    private final NotificationService notificationService;

    public ReminderNotificationScheduler(ReminderRepository reminderRepository,
                                         Clock clock,
                                         NotificationService notificationService) {
        this.reminderRepository = reminderRepository;
        this.clock = clock;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now(clock);

        // First move missed occurrences forward, so they can still be found as due below
        skipMissedOccurrences(now);

        // The database returns only reminders that are not notified yet and due in the next MINUTES
        List<Reminder> dueReminders = reminderRepository
                .findByNotificationSentFalseAndTimeAfterAndTimeLessThanEqual(now, now.plusMinutes(MINUTES));

        for (Reminder reminder : dueReminders) {
            sendNotification(reminder);
        }
    }

    /**
     * A repeating reminder whose time passed without a notification (e.g. the server was down)
     * is moved to its next occurrence without notifying: a late vitamin reminder is just noise.
     */
    private void skipMissedOccurrences(LocalDateTime now) {
        List<Reminder> missedReminders = reminderRepository
                .findByNotificationSentFalseAndRepeatNotAndTimeLessThanEqual(RepeatPattern.NONE, now);

        for (Reminder reminder : missedReminders) {
            rollForwardOrFinish(reminder, now);
            reminderRepository.save(reminder);
        }
    }

    private void sendNotification(Reminder reminder) {
        notificationService.createNotification(reminder);

        rollForwardOrFinish(reminder, reminder.getTime());
        reminderRepository.save(reminder);
    }

    /**
     * Rolls a repeating reminder forward. With no next occurrence (one-time, or past its end date)
     * the reminder is finished: notificationSent stays true.
     */
    private void rollForwardOrFinish(Reminder reminder, LocalDateTime after) {
        Optional<LocalDateTime> next = NextOccurrenceCalculator.nextOccurrenceAfter(reminder, after);

        if (next.isPresent()) {
            reminder.setTime(next.get());
            reminder.setNotificationSent(false);
        } else {
            reminder.setNotificationSent(true);
        }
    }
}
