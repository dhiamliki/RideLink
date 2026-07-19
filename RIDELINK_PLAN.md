# RideLink — Build Plan & Progress

> **Purpose:** my single source of truth for what I'm building and what's done.
> **Legend:** `[ ]` todo · `[~]` in progress · `[x]` done · `[!]` blocked/needs decision

---

## North Star (the full vision — not all built in v1)

A real-time, two-sided carpool marketplace for Tunisia. Drivers post **ride offers**;
passengers post **ride requests**; either side can find and connect with the other.
Route-aware matching, in-app chat, live location. A Spring Boot backend + native
Android app (+ an admin dashboard, later).

The full feature set (matching engine with route-overlap, live GPS tracking, admin
dashboard, wallet, verification, emergency contacts, deployment) is the North Star —
built in later phases, NOT in v1.

**Goal:** the strongest piece in my portfolio — a production-style real-time marketplace
showing backend architecture, real-time systems, geospatial matching, and mobile dev.
Also a plausible startup.

---

## What v1 is (the thin, finishable version — built first)

One complete journey, working end to end:
`sign in (phone) → set up profile → post a ride offer OR a ride request → browse & filter
both → simple match ranking → request a seat → driver accepts/declines → seats update →
chat unlocks.`

Backend (Spring Boot + Postgres) + Android client (Kotlin/Compose). That's it for v1.

### Explicitly DEFERRED (North Star, not v1):
- Admin dashboard (whole separate React app) — Phase 8+
- Live GPS tracking during trips — Phase 6
- Route-overlap matching with waypoints (Tunis→…→Sfax contains Monastir→Sfax) — Phase 5
- Wallet / in-app payments (v1 is cash in person) — later
- Verification badges, emergency contacts, response-rate metrics — later
- Real SMS provider (v1 stubs OTP; swap in a provider later) — see Auth note
- CI/CD, Redis caching, load testing, Play Store publish — Phase 9–10

---

## Architecture & Conventions

- **Two apps in v1:** `backend/` (Spring Boot 3.x, Java 21) and `android/` (Kotlin, Jetpack
  Compose). Admin dashboard is later.
- **Backend = modular monolith**, packages by domain (`auth`, `user`, `ride`, `booking`,
  `proposal`, `chat`, `match`, `safety`, `notification`). Split services later only if a real
  bottleneck demands it.
- **Auth:** phone-number identity + JWT access/refresh. Full phone→OTP→verify→JWT flow, but
  **OTP delivery is stubbed in v1** (dev returns/logs the code; no paid SMS provider yet).
  SMS sending sits behind a single interface so a real provider swaps in as one task.
- **Ride offer & ride request are near-symmetric** (origin, destination, date/time, seats/
  budget, price, notes, posterId) — modelled so shared logic (browse/filter/match) isn't
  duplicated.
- **Matching in v1 is simple and swappable:** rank by same-ish origin/destination + date
  proximity, behind a `MatchingStrategy` interface so the route-overlap version drops in later
  without touching callers.
- **Real-time chat = WebSocket/STOMP.** FCM is reserved for push notifications only.
- **Android:** MVVM, Retrofit (REST), Navigation Compose, Hilt (DI). Google Maps SDK added
  only when maps land (Phase 6-ish).
- **Conventions:** REST under `/api`; DTOs separate from entities; Flyway migrations;
  validation on all inputs; no secrets committed (env vars, `.env.example` checked in).
- **Trust framing (legal):** price is framed as cost-sharing (fuel/tolls split), not paid
  transport — reflected in wording/UX. Confirm Tunisia's rules before real launch.

---

## Phase 0 — Foundation

- [x] Repo: `backend/` + `android/`; root `.gitignore`; README stub
- [x] Spring Boot skeleton (modular-monolith packages), Flyway wired, `GET /api/health`
- [x] Android skeleton (Compose, MVVM, Hilt, Retrofit, Navigation) that calls `/api/health`
- [x] This plan committed as `RIDELINK_PLAN.md`

## Phase 1 — Authentication (phone-first, OTP stubbed)

- [x] User + auth data model; Flyway migration
- [x] Phone signup: request OTP (dev = code logged/returned, no real SMS), verify OTP → account
- [x] JWT access + refresh tokens; refresh endpoint; secure token handling
- [x] Profile: name, photo, bio, languages; `GET/PUT /api/me`
- [x] Android: phone entry → OTP screen → verified → token stored → profile setup
- [x] End to end: a user signs up on the phone and stays logged in

## Phase 2 — Marketplace core (one full journey)

- [x] Ride offer: create/edit/cancel (origin, destination, date, time, seats, price, notes)
- [x] Ride request: create/edit/cancel (origin, destination, time window, budget, notes)
- [x] Browse + search + filter both (date, price, seats) — REST endpoints
- [x] Simple `MatchingStrategy`: rank results by route similarity + date/time
- [x] Booking: passenger requests a seat → driver accepts/declines → seats decrement atomically
- [x] Request-proposal handshake: driver proposes on a request → passenger accepts/declines
- [x] Contact revealed on accept; booking/proposal states (requested/accepted/declined/cancelled)
- [x] Android screens: feed (ranked), post offer, post request, detail, request/accept flow, proposal flow
- [x] `GET /api/offers/mine` + `GET /api/requests/mine` (owner listings, all statuses, with pending counts)
- [x] End to end: post → discover → request → accept → confirmed, on the phone

## Phase 3 — Safety (report/block only — NO ratings)

> Product identity: RideLink is spontaneous peer cost-sharing ("I'm going there anyway,
> I'll take someone and split costs"), NOT a professional/gig driver platform. So there is
> deliberately NO rating or reputation system — it would push toward power-drivers and
> gig-work. Kept: report + block, as a pure safety valve (not a score, not a ranking).
> This also supports the legal framing (cost-sharing, not paid transport).

- [x] Report a user (reason + optional detail); stored for admin review (Phase 8)
- [x] Block a user: a blocked user cannot book/propose/contact you, and drops out of your views
- [x] Enforce blocks across the marketplace (booking, proposal, contact reveal)
- [x] Android: report + block actions, plus a "blocked users" list to unblock
- [x] Profile shows NO rating/review section — just name, photo, bio, member-since

## Phase 4 — Real-time chat + notifications

- [x] WebSocket/STOMP chat between matched users (text); message persistence
- [x] Android: chat screen (WebSocket/STOMP client), conversations list with unread badge
- [~] Read receipts + typing indicator + online presence (read receipts in; typing/presence pending)
- [ ] Push notifications via FCM (new request, accepted, new message, reminder)

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
- [ ] Users, rides, reports, popular routes, analytics

## Phase 9 — Hardening
- [ ] Real SMS provider swapped in; Redis caching; DB indexing; tests; logging; error handling

## Phase 10 — Deployment & showcase
- [ ] Deploy backend; build APK; demo video; architecture diagrams; README; API docs;
      write-up of the matching algorithm

---

## Working log (newest at top)

- 2026-07-19 — Fixed live chat delivery (Android only, no backend/model changes). Root cause: the
  singleton `ChatClient` coupled its whole connection lifecycle to a single `ChatViewModel.onCleared`,
  which fire-and-forget `disconnectAsync()`'d the shared STOMP session. The 4-tab restructure made
  open→back→reopen chat routine (Conversations is now a tab), so a departing screen's teardown
  raced/closed the very session the re-entering screen had just SUBSCRIBEd on — its subscription sat on a
  dead socket, so nothing arrived live until re-entry re-fetched via REST. Fix: reference-count active
  `incoming()` collectors in `ChatClient` (connect on first subscriber via `onStart`, disconnect only
  when the last leaves via `onCompletion`); the reconnect path now drops+nulls the session under the
  mutex and the flow restarts so it re-SUBSCRIBEs after a drop (Reconnecting… self-recovers with capped
  backoff); removed the onCleared teardown from `ChatViewModel`. `assembleDebug` BUILD SUCCESSFUL.
  Two-emulator test: A and B open the same chat → A sends → B sees it LIVE without leaving, and vice
  versa; back out and re-enter still works; kill+restart the backend → "Reconnecting…" clears itself and
  live delivery resumes.

- 2026-07-19 — Owner "my listings" endpoints + My Rides data-source fix. BACKEND: new
  `GET /api/offers/mine` and `GET /api/requests/mine` (auth required; each returns ONLY the caller's own
  rows). Unlike the browse feed these apply NO filtering — every status (ACTIVE/CANCELLED/COMPLETED, and
  ACTIVE/CANCELLED/FULFILLED for requests) and full (availableSeats == 0) listings are included, newest
  first (reusing the existing `findBy{Driver,Passenger}IdOrderByCreatedAtDesc` queries, paginated via
  `PagedResponse.of`). New DTOs `MyOfferResponse`/`MyRequestResponse` embed a `pendingRequestCount` /
  `pendingProposalCount` so the client shows the badge with no extra calls — counts come from one grouped
  query per page. `/mine` is declared before `/{id}` so path routing is unambiguous. No change to the
  public browse endpoints or booking/proposal/chat/safety logic. ANDROID: `MyRidesViewModel` now calls
  `myOffers()`/`myRequests()` instead of filtering the public feed by user id — so the user's FULL and
  CANCELLED listings appear; badges use the embedded counts. Added an owner status chip on each My Rides
  row. `mvnw compile` clean; `assembleDebug` BUILD SUCCESSFUL. Live DB round-trip not run here
  (Docker/Postgres unavailable in this environment).

- 2026-07-19 — UX pass (Android, navigation + wiring only — no API/backend changes): restructured the
  app into **4 bottom-nav tabs**. **Explore** folds the old Home(offers) + Requests tabs into one screen
  with an Offers|Requests segmented toggle (search/filters + Post FAB kept). **Activity** is a new hub
  with a Driver|Passenger segmented control — Driver shows my posted rides (each with a pending
  request-count badge → offer/{id}/requests) and my posted requests (→ request/{id}/proposals); Passenger
  shows my bookings + my proposals. **Messages** promotes the conversations list to a tab with an
  unread-count badge on the tab icon. **Profile** is now account-only (profile card, Blocked users, Log
  out). Messaging: added a **Message** action on the ACCEPTED card of both accept screens via the shared
  onOpenChat path; a successful accept surfaces an "Accepted — Message" snackbar and the inline Message
  button. Refresh: fixed pull-to-refresh on MyBookings/MyProposals/OfferRequests/RequestProposals (real
  refreshing StateFlow), added refresh-on-return to the accept/detail/activity lists, and propagated
  RefreshBus on accept/decline/cancel/withdraw so counterpart browse lists invalidate. No maps/ratings/FCM.
  `assembleDebug` BUILD SUCCESSFUL.

- 2026-07-18 — Phase 4 Android real-time chat screen (WebSocket/STOMP client). Added Krossbow
  (`org.hildan.krossbow` 9.3.0) STOMP over an OkHttp WebSocket transport that reuses the app's shared
  OkHttpClient. `ChatClient` (Hilt singleton) connects to `ws://10.0.2.2:8080/ws` with the stored JWT
  sent as the STOMP CONNECT `Authorization` header, exposes incoming messages on a topic as a Flow that
  reconnects with backoff on a transient drop, and sends messages + read receipts. New MVVM screens: a
  **Conversations** list (counterpart name/photo, last-message preview, unread badge) and a **Chat**
  screen that loads paged history, styles my/their bubbles with local timestamps, sends over STOMP,
  appends incoming live, marks read on open, and handles empty/loading/connection states. Entry points: a
  "Message" button on an ACCEPTED booking and ACCEPTED proposal calls from-booking/from-proposal
  (get-or-create) then opens the chat. `assembleDebug` BUILD SUCCESSFUL. No FCM/push, no typing
  indicators, no backend changes.

- 2026-07-18 — Phase 4 real-time chat backend (Flyway V7 `conversation`/`message`): new
  `com.ridelink.chat` package. Spring WebSocket + STOMP over SockJS at `/ws`; the STOMP CONNECT frame is
  JWT-authenticated (`StompAuthChannelInterceptor` reuses `JwtService`, binds userId as the session
  Principal; invalid/missing token → rejected). A `Conversation` is the single thread between two users
  (stable sorted `pair_key`, unique), linked to the accepted `booking_id` OR `proposal_id`; `Message` has
  content + `read_at` (nullable, doubles as read flag) with index (conversation_id, sent_at). Eligibility
  gate in `ChatService` (accepted booking/proposal connecting the pair AND not blocked via
  `SafetyService`) enforced on get-or-create AND every send/read. REST: GET /api/conversations, GET
  /api/conversations/{id}/messages (paged; 403 non-participant), POST /api/conversations/from-booking/{id}
  + /from-proposal/{id}. STOMP: `/app/chat.send` persists + broadcasts to `/topic/conversations/{id}`;
  `/app/chat.read` marks read + broadcasts a receipt; errors returned to `/user/queue/errors`. Content
  validated (non-empty, ≤2000). Verified with Postgres + dev: `mvnw compile` clean, Flyway V7 applies, and
  a two-client STOMP test passed 20/20 checks (live delivery, history persists, unread + read receipts;
  stranger → 403; invalid token rejected; blocked pair cannot chat).

- 2026-07-17 — Styling pass (Android, visual only — no logic/nav/API changes): introduced a design
  system in the theme layer — refined indigo palette + neutral surfaces + semantic
  success/warning/danger colours (Color.kt), a type scale (Type.kt), rounded shapes (Shape.kt), and
  light/dark schemes wired through RideLinkTheme. Added reusable components in ui/common (Dimens,
  PrimaryButton, SecondaryButton, AppCard, SectionHeader, RideCard, BrandMark, StatusPill, MatchBadge,
  EmptyState, ContactCard) and applied them across every screen. `assembleDebug` BUILD SUCCESSFUL.

- 2026-07-17 — Phase 3 Android report + block UI: extended ApiService (reportUser, blockUser,
  unblockUser, blockedUsers) + models. A reusable `SafetyMenu` (overflow → Report / Block) with a
  reason-picker report dialog and a block confirm dialog. Wired onto ride detail + request detail (only
  when not your own posting) and the counterpart rows of "requests on my ride", "proposals on my
  request", and "my proposals". A Hilt-singleton RefreshBus makes the browse lists reload after any
  block/unblock. New "Blocked users" screen from Profile with per-row Unblock. `assembleDebug` BUILD
  SUCCESSFUL. Two-user manual test passed (report → toast, block → listings vanish, unblock → reappear).

- 2026-07-17 — Phase 3 report + block with enforcement (backend, Flyway V6 `report`/`block`): new
  `com.ridelink.safety` package. Endpoints (token-gated): POST /api/reports (400 on self, stored OPEN,
  201), POST /api/blocks (400 on self, idempotent, 204), DELETE /api/blocks/{blockedUserId} (204), GET
  /api/blocks. Blocks are treated as MUTUAL. Enforcement centralized in `SafetyService`, reused by
  booking, proposal, browse/search + detail (offers & requests hidden; detail → 404), and the
  contact-reveal guard. Creating a block also auto-declines any pending booking/proposal between the
  pair. Verified with Postgres+dev, two users: 21/21 checks passed.

- Plan created. v1 scope locked (auth/marketplace/simple-match/chat); admin/live-GPS/route-overlap/
  wallet/real-SMS deferred.
