-- User + auth data model.
-- gen_random_uuid() (pgcrypto, built into PG13+) supplies UUID defaults.
-- Secrets (OTP codes, refresh tokens) are persisted only as hashes.

create table users (
    id             uuid         primary key default gen_random_uuid(),
    phone_number   varchar(20)  not null,
    phone_verified boolean      not null default false,
    display_name   varchar(100),
    photo_url      varchar(512),
    bio            varchar(500),
    created_at     timestamptz  not null default now()
);

create unique index ux_users_phone_number on users (phone_number);

create table otp_challenge (
    id            uuid         primary key default gen_random_uuid(),
    phone_number  varchar(20)  not null,
    code_hash     varchar(255) not null,
    expires_at    timestamptz  not null,
    consumed      boolean      not null default false,
    attempt_count integer      not null default 0,
    created_at    timestamptz  not null default now()
);

create index ix_otp_challenge_phone_number on otp_challenge (phone_number);

create table refresh_token (
    id         uuid         primary key default gen_random_uuid(),
    user_id    uuid         not null references users (id),
    token_hash varchar(255) not null,
    expires_at timestamptz  not null,
    revoked    boolean      not null default false,
    created_at timestamptz  not null default now()
);

create index ix_refresh_token_user_id on refresh_token (user_id);
