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

- [x] Ride offer: create/edit/cancel (origin, destination, date, time, seats, price, notes)
- [x] Ride request: create/edit/cancel (origin, destination, time window, budget, notes)
- [x] Browse + search + filter both (date, price, seats, rating) — REST endpoints
- [x] Simple `MatchingStrategy`: rank results by route similarity + date/time + rating
- [x] Booking: passenger requests a seat -> driver accepts/declines -> seats decrement atomically
- [x] Contact revealed on accept; booking states (requested/accepted/declined/cancelled)
- [x] Android screens: feed (ranked), post offer, post request, detail, request/accept flow
      (feed + post offer/request done in 2d; detail + request/accept flow done in 2e)
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

- 2026-07-16 — Phase 2 Android ride detail + booking flow (2e): tap a feed card → ride detail
  (full route/date/time/seats/price/notes/smoking-pets/driver+rating). "Request a seat" with a
  seats selector when >1 available → POST booking → "requested" banner; 409 (full/already booked)
  and 403 (own ride) handled as terminal blocked states, and owners see "View requests on this
  ride" instead of the button. My bookings (passenger, from Profile) lists GET /bookings/mine with
  status pill; on ACCEPTED reveals the driver's phone + coordinate note; cancel refreshes the list.
  Requests-on-my-ride (driver, from the owner's ride detail) lists GET /offers/{id}/bookings with
  Accept/Decline per request → refresh reflects seats; passenger contact shown on accept.
  Loading/empty/error + pull-to-refresh throughout; reuses the Retrofit/auth interceptor and the
  indigo theme. Manual two-user test: A posts an offer; B opens it → Request a seat; A opens
  requests on the ride → Accept; both see each other's contact; B cancels → seats restore.
  `./gradlew assembleDebug` clean (pinned Temurin JDK). Booking-list DTOs modeled leniently
  (defaulted/nullable) against the documented contract since backend source wasn't read.
- 2026-07-16 — Phase 2 Android feed + create screens: brand indigo (#5B4FE0) applied as Compose
  theme primary. Bottom-nav shell (Home/Requests/Profile + Post FAB). Feed lists ranked offers
  with driver avatar, route, date/time, seats, price, and a match badge; search bar
  (from/to/date) + filters bottom sheet (max price, min seats) + pull-to-refresh. Requests tab
  lists ride requests symmetrically. Post -> chooser -> Create Offer / Create Request with a
  hardcoded Tunisian-city picker (name+lat/lon), date/time pickers, seats stepper, toggles; on
  submit returns to the feed/requests tab (reloads on entry). Loading/empty/error states
  throughout. Reuses the existing Retrofit/auth interceptor. `./gradlew assembleDebug` clean.
  (Detail + request/accept UI is 2e.) Note: modeled the real backend JSON — nested
  origin/destination objects + PagedResponse — not the flattened shape sketched in the brief.
- 2026-07-16 — Phase 2 booking handshake (Flyway V4 `booking`): request -> accept/decline ->
  cancel, all token-gated with owner/passenger checks. Seats change only on ACCEPT (and restore
  on cancel of an accepted booking) via an atomic conditional UPDATE on `ride_offer`
  (`available_seats >= n` guard, 0 rows -> 409) — verified oversell-safe under a concurrent
  double-accept into the last seat (one OK, one 409, seats=0). Counterpart contact
  (phone + name) revealed to both parties only on ACCEPTED. Verified via curl: full flow +
  own-booking 403, double-book 409, non-owner accept 403, 0-seat 409, contact hidden pre-accept,
  401 without token.
- 2026-07-16 — Phase 2 ride endpoints: offers + requests CRUD (`POST/PUT/DELETE/GET
  /api/offers` + `/api/requests`, owner-only edit/cancel while ACTIVE, soft cancel) and
  browse/search/filter lists (JPA Specifications: originCity/destCity/date/minSeats/maxPrice/
  smoking/pets; active + availableSeats>0) with in-memory pagination. Ranking behind a
  `MatchingStrategy` interface + `SimpleMatchingStrategy` (exact-city > date-closeness > recency)
  returning a 0-100 `matchScore`; DTOs carry the poster's public summary. Verified via curl
  (create/browse/filter include+exclude/edit/request/cancel drop-out/403 owner-only/401).
- 2026-07-16 — Phase 2 data model: `ride_offer` + `ride_request` tables (Flyway V3) with a shared
  embedded `Location` (city_name + lat + lon) for origin/destination; coordinates stored now for
  Phase 5 route matching. Enums for status/time-window, `numeric(10,3)` prices (DT millimes),
  FKs to users, and indexes on (status, date) + city-name columns for 2b browse/filter. JPA
  entities + repositories (finder stubs only, no search logic). Boots + validates + V3 applies
  cleanly. (No dedicated data-model checkbox in Phase 2; the create/edit/cancel boxes are 2b.)
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
