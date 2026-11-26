# Mama Helper – Backend

Mama Helper is an app that helps parents organise their children's daily routines. 
Parents create a profile for each child and add reminders (bath time, doctor appointments, vitamins…). 
Ten minutes before a reminder is due, the backend creates a notification.

This repository contains the **REST API**, built with Spring Boot.
The mobile app (React Native + Expo) lives here: [mama-helper-app](https://github.com/platonfrancesca/mama-helper-app).

## Tech stack

- **Java 21**, **Spring Boot 4.1**
  - Spring Web MVC – REST controllers
  - Spring Data JPA (Hibernate) – database access
  - Bean Validation – request validation
  - Spring Scheduling – periodic reminder checks
- **H2** database, stored in a local file
- **Lombok** – less boilerplate
- **JUnit 6** and **Mockito** – unit and integration tests

## Architecture

The code follows a classic layered architecture:

```
controller  →  service  →  repository  →  entity
(HTTP/JSON)    (business     (Spring Data     (database
               rules)        JPA)             tables)
```

- **Controllers** stay thin: they map HTTP requests to service calls.
- **Services** hold the business rules, e.g. resetting a reminder's notification when its time changes.
- **`GlobalExceptionHandler`** turns exceptions into proper HTTP responses: `400` for invalid input, `404` for missing resources.
- A **`Clock`** is injected wherever time matters, so tests can freeze time.

### How reminder notifications work

`ReminderNotificationScheduler` runs every 60 seconds. For each reminder that is still in the future, has not been notified yet, 
and is due within the next 10 minutes, it:

1. marks the reminder as `notificationSent`, so it is never notified twice
2. creates a `Notification` ("Reminder: Bath time is coming up!")

If a reminder's time is changed, the flag is reset so the reminder will be notified again at its new time.

## Getting started

### Requirements
- Java 21
- No Maven install needed: the project includes the Maven wrapper (`./mvnw`)

### Run the app
```bash
./mvnw spring-boot:run
```
The API runs on http://localhost:8080.

The H2 console is available at http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/mamahelper`, user `sa`, no password).

> Data is stored in the `data/` folder and survives restarts. Delete that folder to start with an empty database. Tests use a separate in-memory database, so they never touch your data.

### Run the tests
```bash
./mvnw test
```

### Try the API
[`requests.http`](requests.http) contains ready-made requests for every endpoint, including invalid ones. 
You can run them directly from IntelliJ IDEA or VS Code (with the REST Client extension).

## API

### Children – `/api/child`

| Method | URL | Description |
|---|---|---|
| `POST` | `/api/child` | Create a child |
| `GET` | `/api/child` | List all children |
| `GET` | `/api/child/{id}` | Get a child by id |
| `PUT` | `/api/child/{id}` | Update a child |
| `DELETE` | `/api/child/{id}` | Delete a child, with all its reminders and notifications |

```json
{ "name": "Maria", "dateOfBirth": "2026-01-01" }
```

### Reminders – `/api/reminders`

| Method | URL | Description |
|---|---|---|
| `POST` | `/api/reminders` | Create a reminder for a child |
| `GET` | `/api/reminders` | List all reminders |
| `GET` | `/api/reminders/{id}` | Get a reminder by id |
| `PUT` | `/api/reminders/{id}` | Update a reminder |
| `DELETE` | `/api/reminders/{id}` | Delete a reminder |

```json
{
  "title": "Bath time",
  "description": "Give baby a bath",
  "time": "2026-12-01T19:00:00",
  "childId": 1
}
```

### Notifications – `/api/notifications`

| Method | URL | Description |
|---|---|---|
| `GET` | `/api/notifications` | List all notifications |
| `GET` | `/api/notifications/{id}` | Get a notification by id |
| `PATCH` | `/api/notifications/{id}/read` | Mark a notification as read |
| `DELETE` | `/api/notifications/{id}` | Delete a notification |

Notifications are created automatically by the scheduler.

### Error responses

All errors use the standard [ProblemDetail](https://www.rfc-editor.org/rfc/rfc9457) format (`application/problem+json`):

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/api/reminders",
  "errors": { "title": "must not be blank", "childId": "must not be null" }
}
```

| Status | When | `title` |
|---|---|---|
| `400` | A field fails validation (details in `errors`) | `Validation failed` |
| `400` | The JSON is malformed or a field has the wrong type | `Malformed request` |
| `404` | The resource or URL does not exist | `Resource not found` |
| `409` | The request conflicts with existing data | `Conflict` |
| `500` | Unexpected server error (no internal details are exposed) | `Internal server error` |

## Roadmap

- Continuous integration with GitHub Actions
