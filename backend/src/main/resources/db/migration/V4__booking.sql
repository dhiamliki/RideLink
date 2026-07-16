-- Booking handshake: a passenger requests seats on an offer; the driver accepts/declines.
-- Seats are decremented only on ACCEPT and restored on cancel of an accepted booking, guarded by
-- an atomic conditional UPDATE on ride_offer.available_seats (see RideOfferRepository) so two
-- passengers can never oversell the last seat.

create table booking (
    id           uuid        primary key default gen_random_uuid(),
    offer_id     uuid        not null references ride_offer (id),
    passenger_id uuid        not null references users (id),
    seats_booked integer     not null default 1,
    status       varchar(20) not null default 'REQUESTED',
    created_at   timestamptz not null default now(),
    decided_at   timestamptz
);

create index ix_booking_offer_status on booking (offer_id, status);
create index ix_booking_passenger on booking (passenger_id);
