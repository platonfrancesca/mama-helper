# 1. Store the repeat rule on the Reminder and roll it forward

Status: Accepted (2026-11-27)

## Context
Parents need repeating reminders (daily vitamins, weekly bath). A Reminder had a single time
and a notificationSent flag, so it could only fire once. The guiding constraint: keep the
backend simple until the mobile app works end to end.

## Decision
Each Reminder stores its repeat pattern (NONE, DAILY, WEEKLY + chosen days), an optional end
date, and the time of its next occurrence. After notifying, the scheduler moves that time to
the following occurrence and clears the flag. Past the end date, the reminder stays finished
(notified, with no next occurrence) until the parent deletes it.

For weekly reminders the parent sends a time of day plus the chosen days, and the backend works
out the first matching occurrence. Edits apply to the whole series and recalculate the next
occurrence from now.

## Alternatives considered
- Generate one row per occurrence in advance: allows editing or skipping a single date, but needs
  a top-up job, more tables, and rewriting future rows on every edit.
- iCalendar RRULE strings: flexible, but needs a library and is hard for the app to build.

## Consequences
- One row per reminder; the existing due-reminder query barely changes.
- The API keeps `time` as the next occurrence and adds `repeat`, `days` and `endDate`, so the
  app's existing list keeps working.
- You can't skip or change a single occurrence (possible later as a small add-on, such as a
  "skip next" field).
- Missed occurrences (server down) are skipped, not sent late.
- Times are server-local wall-clock time (no time zones). That's fine for one family in one zone.
