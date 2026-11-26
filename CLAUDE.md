# Mama Helper – backend

REST API for Mama Helper, an app that helps parents manage children's profiles and daily reminders.
The mobile frontend lives in a separate repo: https://github.com/platonfrancesca/mama-helper-app

## Stack
- Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Validation), Lombok
- H2 in-memory database (`ddl-auto=create`: data is reset on every restart)
- JUnit 6 + Mockito for tests

## Running and testing
- The shell's default `java` is 18, so always set Java 21 for Maven:
  - Tests: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ./mvnw test`
  - Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home ./mvnw spring-boot:run`
- App runs on http://localhost:8080, H2 console at `/h2-console`
- `requests.http` has a manual request for every endpoint; add one for each new endpoint or error case

## Code conventions
- Layers: controller → service → repository → entity. Controllers stay thin; business rules live in services.
- Constructor injection only (no `@Autowired` on fields in main code).
- Missing resources: throw `ResourceNotFoundException` → 404 via `GlobalExceptionHandler`.
- Invalid input: validation annotations on the entity/DTO + `@Valid` in the controller → 400 with field errors.
- Anything time-based uses the injected `Clock`, so tests can use a fixed clock.
- Clean, readable code following these conventions; every service change gets a unit test (Mockito) and existing tests keep passing.

## How to work with me
I use this project to remember programming after maternity leave, especially Java and Spring Boot. I want to learn and improve by using good code practices, and to write and test the code myself where I can.
- Act as a senior developer and mentor: explain concepts and the "why", not just the code.
- Explain a proposed change before making it (or let me write it), and wait for my OK.
- Work in small steps. Test each one (before/after where possible) before moving on.
- Preserve existing working functionality; don't refactor unrelated code without asking.
- Only commit or push when I ask.

## Commit messages
Past-tense sentences describing what changed, e.g.
"Fixed validation error handling so invalid requests return 400 instead of 500. Added test request to requests.http."
