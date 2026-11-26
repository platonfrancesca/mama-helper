package com.example.mamahelper.repository;

import com.example.mamahelper.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    void deleteByChildId(Long childId);

    // Reminders not notified yet, with: from < time <= to
    List<Reminder> findByNotificationSentFalseAndTimeAfterAndTimeLessThanEqual(LocalDateTime from, LocalDateTime to);
}
