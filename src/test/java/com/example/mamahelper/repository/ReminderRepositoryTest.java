package com.example.mamahelper.repository;

import com.example.mamahelper.entity.Child;
import com.example.mamahelper.entity.Reminder;
import com.example.mamahelper.entity.RepeatPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the scheduler's query against the real (H2) database.
 * {@code @Transactional} rolls back the data after every test, so tests don't affect each other.
 */
@SpringBootTest
@Transactional
class ReminderRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 1, 10, 0);
    private static final LocalDateTime IN_TEN_MINUTES = NOW.plusMinutes(10);

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ChildRepository childRepository;

    private Child child;

    @BeforeEach
    void setUp() {
        child = childRepository.save(new Child(null, "Maria", LocalDate.of(2026, 1, 1)));
    }

    @Test
    void shouldFindReminderDueWithinTheNextTenMinutes() {
        Reminder dueIn5 = save("Due in 5 minutes", NOW.plusMinutes(5), false);

        assertEquals(List.of(dueIn5.getId()), findDueIds());
    }

    @Test
    void shouldIncludeReminderExactlyTenMinutesAway() {
        Reminder dueIn10 = save("Due in exactly 10 minutes", IN_TEN_MINUTES, false);

        assertEquals(List.of(dueIn10.getId()), findDueIds());
    }

    @Test
    void shouldNotFindReminderMoreThanTenMinutesAway() {
        save("Due in 15 minutes", NOW.plusMinutes(15), false);

        assertTrue(findDueIds().isEmpty());
    }

    @Test
    void shouldNotFindReminderThatAlreadyPassedOrIsDueNow() {
        save("Passed 1 minute ago", NOW.minusMinutes(1), false);
        save("Due exactly now", NOW, false);

        assertTrue(findDueIds().isEmpty());
    }

    @Test
    void shouldNotFindReminderThatWasAlreadyNotified() {
        save("Already notified", NOW.plusMinutes(5), true);

        assertTrue(findDueIds().isEmpty());
    }

    @Test
    void shouldFindMissedRepeatingReminderButNotMissedOneTimeReminder() {
        Reminder missedDaily = save("Vitamins, missed", NOW.minusHours(2), false);
        missedDaily.setRepeat(RepeatPattern.DAILY);
        save("One-time, passed", NOW.minusHours(2), false);
        Reminder futureDaily = save("Vitamins, later", NOW.plusHours(2), false);
        futureDaily.setRepeat(RepeatPattern.DAILY);
        reminderRepository.flush();

        List<Long> missedIds = reminderRepository
                .findByNotificationSentFalseAndRepeatNotAndTimeLessThanEqual(RepeatPattern.NONE, NOW)
                .stream()
                .map(Reminder::getId)
                .toList();

        assertEquals(List.of(missedDaily.getId()), missedIds);
    }

    private List<Long> findDueIds() {
        return reminderRepository
                .findByNotificationSentFalseAndTimeAfterAndTimeLessThanEqual(NOW, IN_TEN_MINUTES)
                .stream()
                .map(Reminder::getId)
                .toList();
    }

    private Reminder save(String title, LocalDateTime time, boolean notificationSent) {
        Reminder reminder = new Reminder();
        reminder.setTitle(title);
        reminder.setTime(time);
        reminder.setNotificationSent(notificationSent);
        reminder.setChild(child);
        return reminderRepository.save(reminder);
    }
}
