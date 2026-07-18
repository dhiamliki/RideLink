# RideLink

Ride-sharing platform: Spring Boot backend + native Android app. Conventions live in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Prerequisites

- Docker Desktop
- JDK 21+ (builds use the Maven/Gradle wrappers, no local Maven/Gradle needed)
- Android Studio (for the emulator) with an AVD created

## 1. Database

```
docker compose up -d
```

Starts Postgres 16 on `localhost:5433` (db/user/password default to `ridelink`; override via `.env`, see `.env.example`).

## 2. Backend

```
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

Flyway migrates the schema on startup. Verify:

```
curl http://localhost:8080/api/health
# {"status":"ok"}
```

## 3. Android app

```
cd android
./gradlew assembleDebug       # Windows: .\gradlew.bat assembleDebug
```

Requires JDK 21+ (also builds fine on JDK 25). If Gradle selects the wrong JVM (e.g. a JRE without `jlink`), pin the toolchain by setting `org.gradle.java.home` in your user `~/.gradle/gradle.properties` — this stays out of the repo so the project remains portable.

To run in an emulator:

1. Keep the backend running on port 8080.
2. Start an AVD (Android Studio > Device Manager, API 26+), or open `android/` in Android Studio and press Run.
3. CLI install alternative: `./gradlew installDebug`, then launch the RideLink app from the emulator.

The Home screen calls `GET /api/health` through Retrofit using base URL `http://10.0.2.2:8080/` (the emulator's alias for the host's `localhost`) and shows the returned status. Cleartext HTTP is allowed only for `10.0.2.2`/`localhost` in dev.
