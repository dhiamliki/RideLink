# RideLink — Master Build Plan & Progress Checklist

> **Purpose:** single source of truth for what we're building and what's done.
> **Owner of updates:** whoever completes a task ticks its box. The coding agent should
> update this file as part of finishing a task.
> **Legend:** `[ ]` todo · `[~]` in progress · `[x]` done · `[!]` blocked/needs decision

---

## North Star (the full vision — do NOT build all of this first)

A real-time, two-sided carpool marketplace for Tunisia. Drivers post **ride offers**;
passengers post **ride requests**; either side can find and connect with the other.
Intelligent route-aware matching, in-app chat, live location, ratings & trust. A
Spring Boot backend + native Android app (+ an admin dashboard, later).

The full feature set (matching engine with route-overlap, live GPS tracking, admin
dashboard, wallet, verification, emergency contacts, deployment) is the North Star —
built in later phases, NOT in v1.

**Goal:** the strongest piece in the portfolio — a production-style real-time marketplace
showing backend architecture, real-time systems, geospatial matching, and mobile dev.
Also a plausible startup. Primary audience: recruiters (skim) + masters admissions (depth).

---

## What v1 is (the thin, finishable version — build THIS first)

One complete journey, working end to end:
`sign in (phone) → set up profile → post a ride offer OR a ride request → browse & filter
both → simple match ranking → request a seat → driver accepts/declines → seats update →
chat unlocks → after the trip, rate each other.`

Backend (Spring Boot + Postgres) + Android client (Kotlin/Compose). That's it for v1.

### Explicitly DEFERRED (North Star, not v1):
- Admin dashboard (whole separate React app) — Phase 8+
- Live GPS tracking during trips — Phase 6
- Route-overlap matching with waypoints (Tunis→…→Sfax contains Monastir→Sfax) — Phase 4
- Wallet / in-app payments (v1 is cash in person) — later
- Verification badges, emergency contacts, response-rate metrics — later
- Real SMS provider (v1 stubs OTP; swap in a provider later) — see Auth note
- CI/CD, Redis caching, load testing, Play Store publish — Phase 9–10

---

## Architecture & Conventions  *(copy this section into `docs/ARCHITECTURE.md`)*

- **Two apps in v1:** `backend/` (Spring Boot 3.x, Java 21) and `android/` (Kotlin, Jetpack
  Compose). Admin dashboard is later; do not scaffold it now.
- **Backend = modular monolith**, packages by domain (`auth`, `user`, `ride`, `booking`,
  `chat`, `rating`, `match`, `notification`). Split services later only if a real bottleneck
  demands it. (Same discipline that worked before.)
- **Auth:** phone-number identity + JWT access/refresh. Build the full phone→OTP→verify→JWT
  flow, but **stub OTP delivery in v1** (dev returns/logs the code; no paid SMS provider yet).
  Keep SMS sending behind a single interface so a real provider swaps in as one task.
- **Ride offer & ride request are near-symmetric** (origin, destination, date/time, seats/
  budget, price, notes, posterId). Model them so shared logic (browse/filter/match) isn't
  duplicated.
- **Matching in v1 is SIMPLE and swappable:** rank by same-ish origin/destination + date
  proximity + driver rating. Put it behind a `MatchingStrategy` interface so the smart
  route-overlap version drops in later without touching callers.
- **Real-time chat = WebSocket/STOMP** (already a proven skill). FCM is used ONLY for push
  notifications, nothing else.
- **Android:** MVVM, Retrofit (REST), Navigation Compose, Hilt (DI), Room (local cache).
  Google Maps SDK added only when maps land (Phase 6-ish), not in early tasks.
- **Conventions:** REST under `/api`; DTOs separate from entities; Flyway migrations;
  validation on all inputs; no secrets committed (`.env` / env vars, `.env.example` checked in).
- **Trust framing (legal):** price is framed as cost-sharing (fuel/tolls split), not paid
  transport — reflect this in wording/UX. Confirm Tunisia's rules before real launch.

### Deferred on purpose (don't add early)
- No admin dashboard, no live GPS, no waypoint matching, no wallet, no real SMS provider,
  no Redis/CI in early phases. Each is a later phase; adding early = the scope trap.

---

## Phase 0 — Foundation

- [ ] Repo: `backend/` + `android/`; root `.gitignore`; README stub
- [ ] `docs/ARCHITECTURE.md` (paste Architecture & Conventions)
- [ ] `docker-compose.yml`: Postgres
- [ ] Spring Boot skeleton (modular-monolith packages), Flyway wired, `GET /api/health`
- [ ] Android skeleton (Compose, MVVM, Hilt, Retrofit, Navigation) that calls `/api/health`
- [ ] This plan committed as `RIDELINK_PLAN.md`

## Phase 1 — Authentication (phone-first, OTP stubbed)

- [x] User + auth data model; Flyway migration
- [x] Phone signup: request OTP (dev = code logged/returned, no real SMS), verify OTP -> account
- [x] JWT access + refresh tokens; refresh endpoint; secure token handling
- [x] Profile: name, photo (upload -> storage), bio, languages; `GET/PUT /api/me`
- [x] Android: phone entry -> OTP screen -> verified -> token stored -> profile setup
- [ ] End to end: a user signs up on the phone and stays logged in

## Phase 2 — Marketplace core (the soul — one full journey)

- [ ] Ride offer: create/edit/cancel (origin, destination, date, time, seats, price, notes)
- [ ] Ride request: create/edit/cancel (origin, destination, time window, budget, notes)
- [ ] Browse + search + filter both (date, price, seats, rating) — REST endpoints
- [ ] Simple `MatchingStrategy`: rank results by route similarity + date/time + rating
- [ ] Booking: passenger requests a seat -> driver accepts/declines -> seats decrement atomically
- [ ] Contact revealed on accept; booking states (requested/accepted/declined/cancelled)
- [ ] Android screens: feed (ranked), post offer, post request, detail, request/accept flow
- [ ] End to end: post -> discover -> request -> accept -> confirmed, on the phone

## Phase 3 — Trust (minimum viable)

- [ ] Ratings + reviews after a completed trip (stars + comment), aggregated on profile
- [ ] Report user + block user (basic)
- [ ] Trip history per user
- [ ] Android: profile view with rating/reviews, rate-after-trip, report/block

## Phase 4 — Real-time chat + notifications

- [ ] WebSocket/STOMP chat between matched users (text); message persistence
- [ ] Read receipts + typing indicator + online presence
- [ ] Push notifications via FCM (new request, accepted, new message, reminder)
- [ ] Android: chat screen, notification handling

---

## Later phases (North Star — after v1 ships)

## Phase 5 — Intelligent matching (the showpiece upgrade)
- [ ] Route-overlap matching: a trip's path contains a request's sub-trip (waypoints)
- [ ] Distance/pickup-radius calculations, match scoring, ranked recommendations
- [ ] Swap the simple MatchingStrategy for this — no caller changes

## Phase 6 — Maps & live tracking
- [ ] Google Maps: pick origin/destination, view routes on map
- [ ] Live location sharing during an active ride (driver position, ETA, remaining distance)

## Phase 7 — Trust, extended
- [ ] Verification badges, emergency-contact sharing, response-rate/time metrics

## Phase 8 — Admin dashboard (separate React app)
- [ ] Users, rides, reports, popular routes, analytics, completion/rating stats

## Phase 9 — Hardening
- [ ] Real SMS provider swapped in; Redis caching; DB indexing; tests; logging; error handling

## Phase 10 — Deployment & showcase
- [ ] Deploy backend; build APK; demo video; architecture diagrams; README; API docs;
      write-up of the matching algorithm

---

## Working log (append newest at top)

- 2026-07-16 — Phase 1 Android auth flow: Phone -> OTP (dev code prefilled) -> ProfileSetup (new
  user) -> Home greeting, with startup session check and Logout. Tokens in DataStore
  (`TokenStore`); OkHttp `AuthInterceptor` attaches the Bearer token and refreshes once on 401
  (clears + routes to login on failure via `SessionManager`). MVVM + Hilt + Navigation Compose +
  Retrofit; Material 3. `assembleDebug` builds clean. Manual test path documented in the commit.
- 2026-07-16 — Phase 1 profile endpoints: `GET /api/me` (adds `isProfileComplete` =
  displayName set), `PUT /api/me` (displayName required + bio, phone/verified immutable),
  `POST /api/me/photo` (jpeg/png, <=5MB) with local filesystem storage behind a `PhotoStorage`
  interface (cloud swaps in later) served via `GET /api/me/photo/{id}`. DTOs only; all endpoints
  require a valid access token. Verified end-to-end via curl.
- 2026-07-16 — Phase 1 backend auth: phone-OTP request/verify (BCrypt-hashed codes, 5-min TTL,
  30s resend throttle, 5-attempt lockout), JWT access (15m) + rotating refresh (30d, SHA-256
  hashed) with refresh endpoint; JWT security filter (stateless; `/api/auth/**` + `/api/health`
  public). OTP delivery stubbed behind `OtpSender` (`DevOtpSender` logs; `devCode` returned only
  under the `dev` profile). Verified end-to-end via curl.
- _(date)_ — Plan created. v1 scope locked (auth/marketplace/simple-match/chat/ratings);
  admin/live-GPS/route-overlap/wallet/real-SMS deferred. Next: Phase 0.
