# RideLink Architecture

## Repository layout

Two apps live in this monorepo:

- `backend/` — Spring Boot 3.x, Java 21, Maven.
- `android/` — native Android app, Kotlin, Jetpack Compose.

## Backend conventions

- **Style:** modular monolith. Base package `com.ridelink`.
- **Domain packages** (one module each, empty until their phase):
  `auth`, `user`, `ride`, `booking`, `chat`, `rating`, `match`, `notification`.
- **REST:** all endpoints under `/api`.
- **Config:** YAML (`application.yml`); values come from environment variables with dev defaults. No secrets committed.
- **Database:** PostgreSQL 16 via `docker-compose.yml` (host port 5433). Schema is managed by Flyway migrations in `src/main/resources/db/migration` (`V<n>__<desc>.sql`). Hibernate `ddl-auto: validate`.
- **Security:** Spring Security on the classpath but permit-all for now.

## Android conventions

- **Architecture:** MVVM.
- **DI:** Hilt.
- **Networking:** Retrofit for the REST API. Emulator base URL is `http://10.0.2.2:8080/` (maps to host `localhost`).
- **Navigation:** Navigation Compose.
- **Local storage:** Room (added when a feature needs it).
- **Maps:** Google Maps is NOT integrated yet.

## Local development

- `docker compose up -d` starts Postgres.
- Backend reads DB coordinates from `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD` (defaults match the compose file).
- Copy `.env.example` to `.env` to override compose credentials; `.env` is git-ignored.
