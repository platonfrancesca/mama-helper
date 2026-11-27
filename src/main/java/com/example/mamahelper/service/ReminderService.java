package com.example.mamahelper.service;

import com.example.mamahelper.controller.exceptions.ResourceNotFoundException;
import com.example.mamahelper.entity.Child;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.repository.ChildRepository;
import com.example.mamahelper.repository.NotificationRepository;
import com.example.mamahelper.repository.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ChildRepository childRepository;
    private final NotificationRepository notificationRepository;

    public ReminderService(ReminderRepository reminderRepository,
                           ChildRepository childRepository,
                           NotificationRepository notificationRepository) {
        this.reminderRepository = reminderRepository;
        this.childRepository = childRepository;
        this.notificationRepository = notificationRepository;
    }

    public Reminder createReminder(Reminder reminder, Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found with id: " + childId));
        reminder.setChild(child);
        moveToFirstOccurrence(reminder);

        return reminderRepository.save(reminder);
    }

    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    public Reminder getReminderById(Long id) {
        return reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
    }

    public Reminder updateReminder(Long id, Reminder updatedReminder) {
        return reminderRepository.findById(id)
                .map(reminder -> {
                    reminder.setTitle(updatedReminder.getTitle());
                    reminder.setDescription(updatedReminder.getDescription());

                    // An edit always applies to the whole series (see ADR 0001)
                    boolean scheduleChanged = !reminder.getTime().equals(updatedReminder.getTime())
                            || reminder.getRepeat() != updatedReminder.getRepeat()
                            || !Objects.equals(reminder.getDays(), updatedReminder.getDays())
                            || !Objects.equals(reminder.getEndDate(), updatedReminder.getEndDate());

                    if (scheduleChanged) {
                        reminder.setTime(updatedReminder.getTime());
                        reminder.setRepeat(updatedReminder.getRepeat());
                        reminder.setDays(new HashSet<>(updatedReminder.getDays()));
                        reminder.setEndDate(updatedReminder.getEndDate());
                        reminder.setNotificationSent(false);
                        moveToFirstOccurrence(reminder);
                    }

                    return reminderRepository.save(reminder);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + id));
    }

    /**
     * A weekly reminder must land on one of its chosen days: Tuesday 19:00 with days [THURSDAY]
     * becomes Thursday 19:00. A past time is fine: the scheduler skips missed occurrences.
     */
    private void moveToFirstOccurrence(Reminder reminder) {
        NextOccurrenceCalculator.firstOccurrenceFrom(reminder, reminder.getTime())
                .ifPresent(reminder::setTime);
    }

    @Transactional
    public void deleteReminder(Long id) {
        if (!reminderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reminder not found with id: " + id);
        }
        // Notifications reference the reminder (foreign key), so they must be deleted first
        notificationRepository.deleteByReminderId(id);
        reminderRepository.deleteById(id);
    }
}
