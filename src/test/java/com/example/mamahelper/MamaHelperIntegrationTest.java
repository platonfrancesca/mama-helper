package com.example.mamahelper;

import com.example.mamahelper.entity.Child;
import com.example.mamahelper.entity.Notification;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.repository.ChildRepository;
import com.example.mamahelper.repository.NotificationRepository;
import com.example.mamahelper.repository.ReminderRepository;
import com.example.mamahelper.service.ChildService;
import com.example.mamahelper.service.NotificationService;
import com.example.mamahelper.service.ReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MamaHelperIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private ChildService childService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ChildRepository childRepository;

    @Test
    void shouldCreateAndSaveNotificationForReminder() {
        Child child = new Child();
        child.setName("Child 1");
        child.setDateOfBirth(LocalDate.of(2023, 1, 1));
        Child savedChild = childRepository.save(child);

        Reminder reminder = new Reminder();
        reminder.setTitle("Bath Time");
        reminder.setTime(LocalDateTime.now().plusHours(2));
        reminder.setChild(savedChild);
        Reminder savedReminder = reminderRepository.save(reminder);

        notificationService.createNotification(reminder);

        List<Notification> notifications = notificationRepository.findAll();

        assertEquals(1, notifications.size());
        assertEquals(savedReminder.getId(), notifications.get(0).getReminder().getId());

        Notification notification = notifications.get(0);
        assertEquals("Reminder: Bath Time is coming up!", notification.getMessage());
        assertFalse(notification.isRead());
    }

    @Test
    void shouldMarkNotificationAsRead() {
        Child child = new Child();
        child.setName("Child 2");
        child.setDateOfBirth(LocalDate.of(2023, 2, 2));
        Child savedChild = childRepository.save(child);

        Reminder reminder = new Reminder();
        reminder.setTitle("Doctor Appointment");
        reminder.setTime(LocalDateTime.now().plusHours(1));
        reminder.setChild(savedChild);
        Reminder savedReminder = reminderRepository.save(reminder);

        Notification notification = notificationService.createNotification(savedReminder);

        notificationService.markAsRead(notification.getId());

        Notification updatedNotification = notificationRepository.findById(notification.getId())
                .orElseThrow();

        assertTrue(updatedNotification.isRead());
    }

    @Test
    void shouldDeleteReminderThatHasNotifications() {
        Child child = new Child();
        child.setName("Child 4");
        child.setDateOfBirth(LocalDate.of(2023, 4, 4));
        Child savedChild = childRepository.save(child);

        Reminder reminder = new Reminder();
        reminder.setTitle("Vitamins");
        reminder.setTime(LocalDateTime.now().plusMinutes(5));
        reminder.setChild(savedChild);
        Reminder savedReminder = reminderRepository.save(reminder);

        Notification notification = notificationService.createNotification(savedReminder);

        boolean deleted = reminderService.deleteReminder(savedReminder.getId());

        assertTrue(deleted);
        assertFalse(reminderRepository.existsById(savedReminder.getId()));
        assertFalse(notificationRepository.existsById(notification.getId()));
    }

    @Test
    void shouldGetAllNotifications() {
        Child child = new Child();
        child.setName("Child 3");
        child.setDateOfBirth(LocalDate.of(2023, 3, 3));
        Child savedChild = childRepository.save(child);

        Reminder reminder = new Reminder();
        reminder.setTitle("Feeding Time");
        reminder.setTime(LocalDateTime.now().plusDays(1));
        reminder.setChild(savedChild);
        Reminder savedReminder = reminderRepository.save(reminder);

        notificationService.createNotification(savedReminder);

        List<Notification> notifications = notificationService.getAllNotifications();

        assertFalse(notifications.isEmpty());
    }

    @Test
    void shouldDeleteChildWithAllRemindersAndNotifications() {
        Child child = new Child();
        child.setName("Child 5");
        child.setDateOfBirth(LocalDate.of(2023, 5, 5));
        Child savedChild = childRepository.save(child);

        Reminder firstReminder = new Reminder();
        firstReminder.setTitle("Bath Time");
        firstReminder.setTime(LocalDateTime.now().plusMinutes(5));
        firstReminder.setChild(savedChild);
        Reminder savedFirstReminder = reminderRepository.save(firstReminder);

        Reminder secondReminder = new Reminder();
        secondReminder.setTitle("Doctor");
        secondReminder.setTime(LocalDateTime.now().plusDays(2));
        secondReminder.setChild(savedChild);
        Reminder savedSecondReminder = reminderRepository.save(secondReminder);

        Notification notification = notificationService.createNotification(savedFirstReminder);

        childService.deleteChild(savedChild.getId());

        assertFalse(childRepository.existsById(savedChild.getId()));
        assertFalse(reminderRepository.existsById(savedFirstReminder.getId()));
        assertFalse(reminderRepository.existsById(savedSecondReminder.getId()));
        assertFalse(notificationRepository.existsById(notification.getId()));
    }
}
