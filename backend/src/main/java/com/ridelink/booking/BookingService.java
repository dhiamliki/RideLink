package com.ridelink.booking;

import com.ridelink.booking.dto.BookingResponse;
import com.ridelink.booking.dto.ContactDto;
import com.ridelink.ride.RideOffer;
import com.ridelink.ride.RideOfferRepository;
import com.ridelink.ride.RideOfferStatus;
import com.ridelink.safety.SafetyService;
import com.ridelink.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private static final List<BookingStatus> BLOCKING_STATUSES =
            List.of(BookingStatus.REQUESTED, BookingStatus.ACCEPTED);

    private final BookingRepository bookingRepository;
    private final RideOfferRepository offerRepository;
    private final UserRepository userRepository;
    private final SafetyService safetyService;

    public BookingService(BookingRepository bookingRepository, RideOfferRepository offerRepository,
                          UserRepository userRepository, SafetyService safetyService) {
        this.bookingRepository = bookingRepository;
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.safetyService = safetyService;
    }

    @Transactional
    public BookingResponse request(UUID passengerId, UUID offerId, int seatsBooked) {
        RideOffer offer = requireOffer(offerId);
        if (offer.getDriverId().equals(passengerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot book your own offer");
        }
        safetyService.assertNotBlocked(passengerId, offer.getDriverId());
        if (offer.getStatus() != RideOfferStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Offer is not active");
        }
        if (offer.getAvailableSeats() < seatsBooked) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough available seats");
        }
        if (bookingRepository.existsByOfferIdAndPassengerIdAndStatusIn(offerId, passengerId, BLOCKING_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have an active booking on this offer");
        }
        Booking saved = bookingRepository.save(new Booking(offerId, passengerId, seatsBooked));
        return toResponse(saved, offer, passengerId);
    }

    @Transactional
    public BookingResponse accept(UUID driverId, UUID bookingId) {
        Booking booking = requireBooking(bookingId);
        RideOffer offer = requireOffer(booking.getOfferId());
        requireDriver(offer, driverId);
        requireStatus(booking, BookingStatus.REQUESTED);
        safetyService.assertNotBlocked(driverId, booking.getPassengerId());

        // Atomic guarded decrement; 0 rows means the seats are gone -> fail, leave REQUESTED.
        if (offerRepository.decrementAvailableSeats(offer.getId(), booking.getSeatsBooked()) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seats no longer available");
        }
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setDecidedAt(Instant.now());
        Booking saved = bookingRepository.save(booking);
        return toResponse(saved, offer, driverId);
    }

    @Transactional
    public BookingResponse decline(UUID driverId, UUID bookingId) {
        Booking booking = requireBooking(bookingId);
        RideOffer offer = requireOffer(booking.getOfferId());
        requireDriver(offer, driverId);
        requireStatus(booking, BookingStatus.REQUESTED);

        booking.setStatus(BookingStatus.DECLINED);
        booking.setDecidedAt(Instant.now());
        return toResponse(bookingRepository.save(booking), offer, driverId);
    }

    @Transactional
    public BookingResponse cancel(UUID passengerId, UUID bookingId) {
        Booking booking = requireBooking(bookingId);
        if (!booking.getPassengerId().equals(passengerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        if (booking.getStatus() != BookingStatus.REQUESTED && booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking cannot be cancelled");
        }
        // Restore seats only if they had been reserved (ACCEPTED).
        if (booking.getStatus() == BookingStatus.ACCEPTED) {
            offerRepository.restoreAvailableSeats(booking.getOfferId(), booking.getSeatsBooked());
        }
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        return toResponse(saved, requireOffer(booking.getOfferId()), passengerId);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> mine(UUID passengerId) {
        List<Booking> bookings = bookingRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
        Map<UUID, RideOffer> offers = offersById(bookings.stream().map(Booking::getOfferId).toList());
        return bookings.stream()
                .map(b -> toResponse(b, offers.get(b.getOfferId()), passengerId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> forOffer(UUID driverId, UUID offerId) {
        RideOffer offer = requireOffer(offerId);
        requireDriver(offer, driverId);
        return bookingRepository.findByOfferIdOrderByCreatedAtDesc(offerId).stream()
                .map(b -> toResponse(b, offer, driverId))
                .toList();
    }

    // Reveals the counterpart's contact (driver <-> passenger) only on ACCEPTED bookings.
    private BookingResponse toResponse(Booking booking, RideOffer offer, UUID viewerId) {
        ContactDto contact = null;
        if (booking.getStatus() == BookingStatus.ACCEPTED && offer != null) {
            UUID counterpartId = viewerId.equals(booking.getPassengerId())
                    ? offer.getDriverId()
                    : booking.getPassengerId();
            // Defensive: never reveal a blocked counterpart's contact even on an old ACCEPTED booking.
            if (!safetyService.isBlockedBetween(viewerId, counterpartId)) {
                contact = userRepository.findById(counterpartId)
                        .map(u -> new ContactDto(u.getDisplayName(), u.getPhoneNumber()))
                        .orElse(null);
            }
        }
        return BookingResponse.of(booking, contact);
    }

    private Map<UUID, RideOffer> offersById(List<UUID> ids) {
        return offerRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(RideOffer::getId, Function.identity(), (a, b) -> a));
    }

    private Booking requireBooking(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private RideOffer requireOffer(UUID id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
    }

    private void requireDriver(RideOffer offer, UUID driverId) {
        if (!offer.getDriverId().equals(driverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your offer");
        }
    }

    private void requireStatus(Booking booking, BookingStatus expected) {
        if (booking.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is not " + expected);
        }
    }
}
