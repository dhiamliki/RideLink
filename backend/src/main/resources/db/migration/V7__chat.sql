-- Real-time chat (Phase 4, 4a). A conversation is the private thread between exactly two users,
-- unlocked by an ACCEPTED booking OR an ACCEPTED proposal connecting them. The pair_key (the two
-- participant ids sorted and joined) is unique so the SAME two users always share ONE conversation,
-- regardless of how many accepted connections they have. Eligibility (accepted connection + not
-- blocked) is enforced in com.ridelink.chat.ChatService on both creation and message send.

create table conversation (
    id            uuid        primary key default gen_random_uuid(),
    pair_key      varchar(73) not null,
    participant_a uuid        not null references users (id),
    participant_b uuid        not null references users (id),
    booking_id    uuid        references booking (id),
    proposal_id   uuid        references request_proposal (id),
    created_at    timestamptz not null default now(),
    constraint ck_conversation_link check (booking_id is not null or proposal_id is not null)
);

create unique index ux_conversation_pair on conversation (pair_key);

create table message (
    id              uuid          primary key default gen_random_uuid(),
    conversation_id uuid          not null references conversation (id),
    sender_id       uuid          not null references users (id),
    content         varchar(2000) not null,
    sent_at         timestamptz   not null default now(),
    read_at         timestamptz
);

create index ix_message_conversation_sent on message (conversation_id, sent_at);
