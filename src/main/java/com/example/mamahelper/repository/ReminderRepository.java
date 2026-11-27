package com.example.mamahelper.repository;

import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    void deleteByChildId(Long childId);

    // Reminders not notified yet, with: from < time <= to
    List<Reminder> findByNotificationSentFalseAndTimeAfterAndTimeLessThanEqual(LocalDateTime from, LocalDateTime to);

    // Missed occurrences: not notified yet, time already passed, and the reminder repeats (repeat != NONE)
    List<Reminder> findByNotificationSentFalseAndRepeatNotAndTimeLessThanEqual(RepeatPattern repeat, LocalDateTime now);
}
