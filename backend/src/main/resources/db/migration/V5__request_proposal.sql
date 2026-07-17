-- Request-proposal handshake (mirror of the booking handshake, roles flipped): a DRIVER proposes to
-- fulfill a passenger's ride_request; the request owner accepts/declines. Accepting marks the
-- ride_request FULFILLED (dropping it from the ACTIVE browse list) and auto-declines the request's
-- other still-pending proposals, all in one transaction (see ProposalService).

create table request_proposal (
    id             uuid          primary key default gen_random_uuid(),
    request_id     uuid          not null references ride_request (id),
    driver_id      uuid          not null references users (id),
    status         varchar(20)   not null default 'PROPOSED',
    message        varchar(500),
    price_per_seat numeric(10,3),
    created_at     timestamptz   not null default now(),
    decided_at     timestamptz
);

create index ix_request_proposal_request_status on request_proposal (request_id, status);
create index ix_request_proposal_driver on request_proposal (driver_id);
