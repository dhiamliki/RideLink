-- Ride offer (driver) + ride request (passenger) data model.
--
-- Modeling choice: two tables sharing an embedded Location (city_name + lat + lon) for both
-- origin and destination. Offer and request are near-symmetric on the browse/filter/match
-- surface (geo endpoints + date + status), but differ enough elsewhere (seats/price semantics,
-- terminal status name) that a single discriminator table would force many nullable columns and
-- a blurred status set. Two tables keep each side's NOT NULL constraints meaningful; the shared
-- geo columns are identical in name/type so cross-type search logic (2b) and Phase 5 route
-- matching can treat them uniformly.
--
-- Coordinates are stored now (double precision) even though v1 matches A->B only, so route
-- matching lands without a schema change. Prices use numeric(10,3): Tunisian Dinar carries three
-- decimal places (millimes).

create table ride_offer (
    id               uuid          primary key default gen_random_uuid(),
    driver_id        uuid          not null references users (id),
    status           varchar(20)   not null default 'ACTIVE',
    origin_city_name varchar(120)  not null,
    origin_lat       double precision not null,
    origin_lon       double precision not null,
    dest_city_name   varchar(120)  not null,
    dest_lat         double precision not null,
    dest_lon         double precision not null,
    departure_date   date          not null,
    departure_time   time          not null,
    total_seats      integer       not null,
    available_seats  integer       not null,
    price_per_seat   numeric(10, 3) not null,
    notes            varchar(500),
    smoking_allowed  boolean,
    pets_allowed     boolean,
    created_at       timestamptz   not null default now()
);

create index ix_ride_offer_status_departure_date on ride_offer (status, departure_date);
create index ix_ride_offer_origin_city on ride_offer (origin_city_name);
create index ix_ride_offer_dest_city on ride_offer (dest_city_name);
create index ix_ride_offer_driver_id on ride_offer (driver_id);

create table ride_request (
    id                    uuid          primary key default gen_random_uuid(),
    passenger_id          uuid          not null references users (id),
    status                varchar(20)   not null default 'ACTIVE',
    origin_city_name      varchar(120)  not null,
    origin_lat            double precision not null,
    origin_lon            double precision not null,
    dest_city_name        varchar(120)  not null,
    dest_lat              double precision not null,
    dest_lon              double precision not null,
    preferred_date        date          not null,
    preferred_time_window varchar(20)   not null,
    seats_needed          integer       not null default 1,
    max_price_per_seat    numeric(10, 3),
    notes                 varchar(500),
    created_at            timestamptz   not null default now()
);

create index ix_ride_request_status_preferred_date on ride_request (status, preferred_date);
create index ix_ride_request_origin_city on ride_request (origin_city_name);
create index ix_ride_request_dest_city on ride_request (dest_city_name);
create index ix_ride_request_passenger_id on ride_request (passenger_id);
