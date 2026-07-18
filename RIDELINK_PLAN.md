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

- [ ] User + auth data model; Flyway migration
- [ ] Phone signup: request OTP (dev = code logged/returned, no real SMS), verify OTP -> account
- [ ] JWT access + refresh tokens; refresh endpoint; secure token handling
- [ ] Profile: name, photo (upload -> storage), bio, languages; `GET/PUT /api/me`
- [ ] Android: phone entry -> OTP screen -> verified -> token stored -> profile setup
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

## Phase 3 — Safety (report/block only — NO ratings)

> Product identity: RideLink is spontaneous peer cost-sharing ("I'm going there anyway,
> I'll take someone and split costs"), NOT a professional/gig driver platform. So there is
> deliberately NO rating or reputation system — it would push toward power-drivers and
> gig-work. Kept: report + block, as a pure safety valve (not a score, not a ranking).
> This also supports the legal framing (cost-sharing, not paid transport).

- [x] Report a user (reason + optional detail); stored for admin review (Phase 8) *(backend, 3a)*
- [x] Block a user: a blocked user cannot book/propose/contact you, and drops out of your views *(backend, 3a)*
- [x] Enforce blocks across the marketplace (booking, proposal, contact reveal) *(backend, 3a)*
- [x] Android: report + block actions (from a user's info / an accepted connection), a "blocked
      users" list to unblock *(3b)*
- [ ] (Profile shows NO rating/review section — just name, photo, bio, member-since)

## Phase 4 — Real-time chat + notifications

- [x] WebSocket/STOMP chat between matched users (text); message persistence *(backend, 4a)*
- [ ] Read receipts + typing indicator + online presence
- [ ] Push notifications via FCM (new request, accepted, new message, reminder)
- [x] Android: chat screen (WebSocket/STOMP client) *(4b — notification handling deferred to FCM task)*

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

- 2026-07-19 — Owner "my listings" endpoints + My Rides data-source fix. BACKEND: new
  `GET /api/offers/mine` and `GET /api/requests/mine` (auth required via `anyRequest().authenticated()`;
  each returns ONLY the caller's own rows). Unlike the browse feed these apply NO filtering — every
  status (ACTIVE/CANCELLED/COMPLETED, and ACTIVE/CANCELLED/FULFILLED for requests) and full
  (availableSeats == 0) listings are included, newest first (reusing the existing
  `findBy{Driver,Passenger}IdOrderByCreatedAtDesc` repo queries, paginated via `PagedResponse.of`). New
  DTOs `MyOfferResponse`/`MyRequestResponse` embed a `pendingRequestCount` / `pendingProposalCount` so the
  client shows the badge with no extra calls — counts come from one grouped query per page
  (`countPendingByOfferIds` = REQUESTED bookings, `countPendingByRequestIds` = PROPOSED proposals). `/mine`
  is declared before `/{id}` so path routing is unambiguous. No change to the public browse endpoints or
  booking/proposal/chat/safety logic. ANDROID: `MyRidesViewModel` now calls `myOffers()`/`myRequests()`
  instead of filtering the public feed by user id — so the user's FULL and CANCELLED listings appear;
  badges use the embedded counts (dropped the per-offer bookings calls and the `me()` lookup). Added a
  small owner status chip (Active/Full/Cancelled/Completed · Active/Fulfilled) on each My Rides row.
  `./mvnw compile` clean; `./gradlew assembleDebug` BUILD SUCCESSFUL on the pinned JDK. Live DB round-trip
  (create/cancel/full → GET /mine) not run here — Docker/Postgres unavailable in this environment.

- 2026-07-19 — UX pass (Android, navigation + wiring only — no API/backend changes): restructured the
  app into **4 bottom-nav tabs**. **Explore** folds the old Home(offers) + Requests tabs into one screen
  with an Offers|Requests segmented toggle (search/filters + Post FAB kept). **Activity** is a new hub
  with a Driver|Passenger segmented control — Driver shows *my posted rides* (each with a pending
  request-count badge → offer/{id}/requests) and *my posted requests* (→ request/{id}/proposals);
  Passenger shows my bookings + my proposals. NOTE: no backend "my offers"/"my requests" endpoint
  exists, so MyRides reads the public feeds and filters to the current user's own posts (pending counts
  from the existing per-offer bookings endpoint) — closest available, no backend invented. **Messages**
  promotes the conversations list to a tab with an unread-count badge on the tab icon. **Profile** is now
  account-only (profile card, Blocked users, Log out) — the old activity button-list moved to the
  Activity tab. Messaging: added a **Message** action on the ACCEPTED card of both accept screens
  (OfferRequests / RequestProposals) via the shared onOpenChat(conversationId, name) path; on a
  successful accept a "Accepted — Message" snackbar surfaces the get-or-create conversation and the inline
  Message button appears. Refresh: fixed the fake pull-to-refresh on MyBookings/MyProposals/OfferRequests/
  RequestProposals (real refreshing StateFlow, Feed template), added refresh-on-return
  (LaunchedEffect{load()}) to the accept/detail/activity lists, and propagated RefreshBus on
  accept/decline/cancel/withdraw so counterpart browse lists invalidate. No live WebSocket push (separate
  task), no maps/ratings/FCM. `./gradlew assembleDebug` BUILD SUCCESSFUL on the pinned JDK.

- 2026-07-18 — Phase 4 Android real-time chat screen (4b, WebSocket/STOMP client). Added Krossbow
  (`org.hildan.krossbow` 9.3.0) STOMP over an OkHttp WebSocket transport that reuses the app's shared
  OkHttpClient. `ChatClient` (Hilt singleton) connects to `ws://10.0.2.2:8080/ws` with the stored JWT
  sent as the STOMP CONNECT `Authorization` header, exposes incoming messages on a topic as a Flow that
  reconnects with backoff on a transient drop (Connecting/Connected/Reconnecting/Disconnected state),
  sends messages + read receipts, and is torn down on chat close (no leaked sockets). New MVVM screens:
  a **Conversations** list (from Profile → Messages) with counterpart name/photo, last-message preview
  and an unread badge; a **Chat** screen that loads paged history, styles my/their bubbles (brand indigo
  for mine) with local timestamps, sends over STOMP, appends incoming live, marks read on open, and
  handles empty ("Say hello")/loading/connection states. **Entry points:** a "Message" button on an
  ACCEPTED booking (My bookings) and ACCEPTED proposal (My proposals) calls from-booking/from-proposal
  (get-or-create) then opens the chat. Chat REST models verified against the running backend (dev):
  conversation id is `id`, counterpart `{id,displayName?,photoUrl?}`, paged messages
  `{content:[{id,senderId,content,sentAt,readAt?}],page,size,totalElements,totalPages}`.
  `./gradlew assembleDebug` BUILD SUCCESSFUL on the pinned JDK. No FCM/push, no typing indicators, no
  backend changes.

  TWO-EMULATOR manual test (both signed in against the same dev backend on host :8080, with an ACCEPTED
  booking between A and B — A booked B's offer, B accepted): (1) emulator2 = user B → Profile → Messages
  → open the conversation (chat screen stays open). (2) emulator1 = user A → Profile → My bookings →
  the ACCEPTED booking → **Message** → type and send. (3) The message appears on B's open chat LIVE
  (no refresh). (4) B replies → appears live on A. (5) Reopen either chat → history persists (GET
  messages). (6) With B's chat closed, A sends → B's Profile → Messages shows an unread badge on the
  conversation row; opening it clears the badge.

- 2026-07-18 — Phase 4 real-time chat backend (4a, Flyway V7 `conversation`/`message`): new
  `com.ridelink.chat` package. Spring WebSocket + STOMP over SockJS at `/ws`; the STOMP CONNECT frame
  is JWT-authenticated (`StompAuthChannelInterceptor` reuses `JwtService`, binds userId as the session
  Principal; invalid/missing token → connection rejected). A `Conversation` is the single thread between
  two users (stable sorted `pair_key`, unique), linked to the accepted `booking_id` OR `proposal_id`;
  `Message` has content + `read_at` (nullable, doubles as read flag) with index (conversation_id,
  sent_at). Eligibility gate in `ChatService` (accepted booking/proposal connecting the pair AND not
  blocked via `SafetyService`) enforced on get-or-create AND every send/read. REST: GET
  /api/conversations (counterpart + last message + unread count), GET /api/conversations/{id}/messages
  (paged; 403 non-participant), POST /api/conversations/from-booking/{id} + /from-proposal/{id}
  (get-or-create, verifies acceptance + ownership). STOMP: `/app/chat.send` persists + broadcasts to
  `/topic/conversations/{id}`; `/app/chat.read` marks read + broadcasts a receipt to
  `…/{id}/read`; errors returned to `/user/queue/errors`. Content validated (non-empty, ≤2000). Verified
  with Postgres + dev: `./mvnw compile` clean, Flyway V7 applies (success), and a two-client STOMP test
  — 20/20 checks: two users with an ACCEPTED booking connect with their JWTs, A↔B messages delivered
  live, history persists (GET shows 2), unread count + read receipts work; a stranger with no accepted
  connection gets 403 (create + read), an invalid token is rejected at CONNECT, and a blocked pair
  cannot chat (create → 403, STOMP send not persisted + error frame, conversation hidden). No FCM/push,
  no typing indicators, no Android — those are later.

- 2026-07-17 — Styling pass (Android, visual only — no logic/nav/API changes): introduced a proper
  design system in the theme layer — refined indigo palette + neutral surfaces + semantic
  success/warning/danger colours (Color.kt), a clear type scale (Type.kt), rounded shapes (Shape.kt),
  and light/dark schemes wired through RideLinkTheme. Added reusable components in ui/common
  (Dimens spacing scale, PrimaryButton, SecondaryButton, AppCard, SectionHeader, RideCard, BrandMark,
  refined StatusPill (colour-by-status), MatchBadge, icon EmptyState, ContactCard). Applied across every
  screen: feed + requests now share RideCard; auth (phone/OTP/profile setup) got a brand mark + primary
  buttons; create offer/request use grouped section cards; ride/request detail, my bookings, my proposals,
  requests-on-my-ride, proposals-on-my-request, blocked users all use the shared card + pills + primary/
  secondary buttons; profile redesigned with a header card + grouped actions; neutral app background so
  cards pop; top bar + bottom nav on surface. `./gradlew assembleDebug` BUILD SUCCESSFUL on the pinned JDK.

- 2026-07-17 — Phase 3 Android report + block UI (3b): extended ApiService (reportUser, blockUser,
  unblockUser, blockedUsers) + models. A reusable `SafetyMenu` (overflow → Report / Block) with a
  reason-picker report dialog (HARASSMENT/UNSAFE_DRIVING/NO_SHOW/INAPPROPRIATE/OTHER + optional detail,
  confirmation toast) and a block confirm dialog. Wired onto ride detail + request detail (top-bar, only
  when not your own posting) and the counterpart rows of "requests on my ride", "proposals on my request",
  and "my proposals". Blocking from a detail screen navigates back; a Hilt-singleton RefreshBus makes the
  Feed/Requests browse lists reload after any block/unblock so hidden content updates. New "Blocked users"
  screen from Profile lists GET /api/blocks (name+photo) with per-row Unblock (DELETE) and an empty state.
  Verified response shapes against the running backend (reports→201, blocks→204 idempotent, GET blocks→
  [{id,displayName,photoUrl}], DELETE→204). `./gradlew assembleDebug` BUILD SUCCESSFUL on the pinned JDK.
  Two-user manual test: A opens B's ride → Report (pick reason → toast) and Block B → B's listings vanish
  from A's browse; A: Profile → Blocked users → sees B → Unblock → B reappears. (MyBookings has no report/
  block since that payload carries no counterpart user id.)

- 2026-07-17 — Phase 3 report + block with enforcement (3a, backend, Flyway V6 `report`/`block`):
  new `com.ridelink.safety` package. Endpoints (token-gated): POST /api/reports (400 on self, stored
  OPEN for future admin review, 201), POST /api/blocks (400 on self, idempotent, 204), DELETE
  /api/blocks/{blockedUserId} (204), GET /api/blocks (blocked users' id+name+photo). Blocks are
  treated as MUTUAL for protection. Enforcement centralized in `SafetyService` (assertNotBlocked /
  isBlockedBetween / blockedUserIdsFor), reused by booking (request+accept), proposal (propose+accept),
  browse/search + detail (offers & requests hidden; detail → 404), and the contact-reveal guard.
  Creating a block also auto-declines any pending booking (REQUESTED) / proposal (PROPOSED) between the
  pair so an in-flight handshake can't be accepted after the block. Verified with Postgres+dev, two
  users: 21/21 checks — block hides listings both ways, book/propose across a block → 403, detail →
  404, report/block self → 400, no token → 401, list/unblock restores visibility + booking.

- _(date)_ — Plan created. v1 scope locked (auth/marketplace/simple-match/chat/ratings);
  admin/live-GPS/route-overlap/wallet/real-SMS deferred. Next: Phase 0.