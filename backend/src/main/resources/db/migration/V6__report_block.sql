-- Safety: report + block (Phase 3, report/block only — no ratings). A report is filed for later
-- admin review (Phase 8). A block protects in BOTH directions and is enforced across booking,
-- proposal, and browse by com.ridelink.safety.SafetyService.

create table report (
    id               uuid         primary key default gen_random_uuid(),
    reporter_id      uuid         not null references users (id),
    reported_user_id uuid         not null references users (id),
    reason           varchar(30)  not null,
    detail           varchar(1000),
    status           varchar(20)  not null default 'OPEN',
    created_at       timestamptz  not null default now()
);

create index ix_report_reported_user on report (reported_user_id);

create table block (
    id              uuid        primary key default gen_random_uuid(),
    blocker_id      uuid        not null references users (id),
    blocked_user_id uuid        not null references users (id),
    created_at      timestamptz not null default now()
);

create unique index ux_block_pair on block (blocker_id, blocked_user_id);
