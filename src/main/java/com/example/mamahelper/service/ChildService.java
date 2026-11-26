package com.example.mamahelper.service;

import com.example.mamahelper.controller.exceptions.ResourceNotFoundException;
import com.example.mamahelper.entity.Child;
import com.example.mamahelper.repository.ChildRepository;
import com.example.mamahelper.repository.NotificationRepository;
import com.example.mamahelper.repository.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChildService {

    private final ChildRepository childRepository;
    private final ReminderRepository reminderRepository;
    private final NotificationRepository notificationRepository;

    public ChildService(ChildRepository childRepository,
                        ReminderRepository reminderRepository,
                        NotificationRepository notificationRepository) {
        this.childRepository = childRepository;
        this.reminderRepository = reminderRepository;
        this.notificationRepository = notificationRepository;
    }

    public Child createChild(Child child) {
        return childRepository.save(child);
    }

    public List<Child> getAllChildren() {
        return childRepository.findAll();
    }

    public Child getChildById(Long id) {
        return childRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found with id: " + id));
    }

    public Child updateChildInfo(Long id, Child updatedChildInfo) {
        return childRepository.findById(id)
                .map(child -> {
                    child.setName(updatedChildInfo.getName());
                    child.setDateOfBirth(updatedChildInfo.getDateOfBirth());

                    return childRepository.save(child);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Child not found with id: " + id));
    }

    @Transactional
    public void deleteChild(Long id) {
        if (!childRepository.existsById(id)) {
            throw new ResourceNotFoundException("Child not found with id: " + id);
        }
        // Delete from the "bottom" up, because of the foreign keys:
        // notifications -> reminders -> child
        notificationRepository.deleteByReminderChildId(id);
        reminderRepository.deleteByChildId(id);
        childRepository.deleteById(id);
    }
}
