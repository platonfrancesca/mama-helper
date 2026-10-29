package com.example.mamahelper.service;

import com.example.mamahelper.entity.Child;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.repository.ChildRepository;
import com.example.mamahelper.repository.ReminderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ChildRepository childRepository;

    public ReminderService(ReminderRepository reminderRepository,
                           ChildRepository childRepository) {
        this.reminderRepository = reminderRepository;
        this.childRepository = childRepository;
    }

    public Reminder createReminder(Reminder reminder, Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));
        reminder.setChild(child);

        return reminderRepository.save(reminder);
    }

    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    public Optional<Reminder> getReminderById(Long id) {
        return reminderRepository.findById(id);
    }

    public Optional<Reminder> updateReminder(Long id, Reminder updatedReminder) {
        return reminderRepository.findById(id)
                .map(reminder -> {
                    reminder.setTitle(updatedReminder.getTitle());
                    reminder.setDescription(updatedReminder.getDescription());

                    if (!reminder.getTime().equals(updatedReminder.getTime())) {
                        reminder.setNotificationSent(false);
                    }

                    reminder.setTime(updatedReminder.getTime());
                    return reminderRepository.save(reminder);
                });
    }

    public boolean deleteReminder(Long id) {
        if (!reminderRepository.existsById(id)) {
            return false;
        }
        reminderRepository.deleteById(id);
        return true;
    }
}
