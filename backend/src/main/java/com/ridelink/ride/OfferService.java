package com.ridelink.ride;

import com.ridelink.ride.dto.OfferForm;
import com.ridelink.ride.dto.OfferResponse;
import com.ridelink.ride.dto.PagedResponse;
import com.ridelink.ride.dto.UserSummary;
import com.ridelink.ride.match.MatchCandidate;
import com.ridelink.ride.match.MatchQuery;
import com.ridelink.ride.match.MatchingStrategy;
import com.ridelink.safety.SafetyService;
import com.ridelink.user.User;
import com.ridelink.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OfferService {

    private final RideOfferRepository offerRepository;
    private final UserRepository userRepository;
    private final MatchingStrategy matchingStrategy;
    private final SafetyService safetyService;

    public OfferService(RideOfferRepository offerRepository, UserRepository userRepository,
                        MatchingStrategy matchingStrategy, SafetyService safetyService) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.matchingStrategy = matchingStrategy;
        this.safetyService = safetyService;
    }

    @Transactional
    public OfferResponse create(UUID driverId, OfferForm form) {
        RideOffer offer = new RideOffer(driverId, form.origin().toLocation(), form.destination().toLocation(),
                form.departureDate(), form.departureTime(), form.totalSeats(), form.pricePerSeat());
        applyEditableFields(offer, form);
        RideOffer saved = offerRepository.save(offer);
        return OfferResponse.of(saved, driverSummary(driverId), null);
    }

    @Transactional
    public OfferResponse update(UUID driverId, UUID offerId, OfferForm form) {
        RideOffer offer = requireOffer(offerId);
        requireOwner(offer, driverId);
        if (offer.getStatus() != RideOfferStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only active offers can be edited");
        }
        int booked = offer.getTotalSeats() - offer.getAvailableSeats();
        int newAvailable = form.totalSeats() - booked;
        if (newAvailable < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Total seats cannot drop below booked seats");
        }
        offer.setOrigin(form.origin().toLocation());
        offer.setDestination(form.destination().toLocation());
        offer.setDepartureDate(form.departureDate());
        offer.setDepartureTime(form.departureTime());
        offer.setTotalSeats(form.totalSeats());
        offer.setAvailableSeats(newAvailable);
        applyEditableFields(offer, form);
        return OfferResponse.of(offer, driverSummary(driverId), null);
    }

    @Transactional
    public void cancel(UUID driverId, UUID offerId) {
        RideOffer offer = requireOffer(offerId);
        requireOwner(offer, driverId);
        if (offer.getStatus() != RideOfferStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only active offers can be cancelled");
        }
        offer.setStatus(RideOfferStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public OfferResponse get(UUID viewerId, UUID offerId) {
        RideOffer offer = requireOffer(offerId);
        // Hide a blocked user's offer from existence (avoid leaking that it exists).
        if (safetyService.isBlockedBetween(viewerId, offer.getDriverId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found");
        }
        return OfferResponse.of(offer, driverSummary(offer.getDriverId()), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OfferResponse> search(UUID viewerId, String originCity, String destCity, LocalDate date,
                                               Integer minSeats, BigDecimal maxPrice, Boolean smokingAllowed,
                                               Boolean petsAllowed, int page, int size) {
        Specification<RideOffer> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("status"), RideOfferStatus.ACTIVE));
            ps.add(cb.greaterThan(root.<Integer>get("availableSeats"), 0));
            if (hasText(originCity)) {
                ps.add(cb.equal(cb.lower(root.get("origin").<String>get("cityName")), originCity.trim().toLowerCase()));
            }
            if (hasText(destCity)) {
                ps.add(cb.equal(cb.lower(root.get("destination").<String>get("cityName")), destCity.trim().toLowerCase()));
            }
            if (date != null) {
                ps.add(cb.equal(root.get("departureDate"), date));
            }
            if (minSeats != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<Integer>get("availableSeats"), minSeats));
            }
            if (maxPrice != null) {
                ps.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("pricePerSeat"), maxPrice));
            }
            if (smokingAllowed != null) {
                ps.add(cb.equal(root.get("smokingAllowed"), smokingAllowed));
            }
            if (petsAllowed != null) {
                ps.add(cb.equal(root.get("petsAllowed"), petsAllowed));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Set<UUID> blocked = safetyService.blockedUserIdsFor(viewerId);
        List<RideOffer> found = offerRepository.findAll(spec).stream()
                .filter(o -> !blocked.contains(o.getDriverId()))
                .toList();
        MatchQuery query = new MatchQuery(originCity, destCity, date);
        Map<UUID, UserSummary> drivers = summaries(found.stream().map(RideOffer::getDriverId).toList());

        List<OfferResponse> ranked = found.stream()
                .map(o -> Map.entry(o, matchingStrategy.score(query, candidate(o))))
                .sorted(Comparator.<Map.Entry<RideOffer, Integer>>comparingInt(Map.Entry::getValue).reversed()
                        .thenComparing(e -> e.getKey().getCreatedAt(), Comparator.reverseOrder()))
                .map(e -> OfferResponse.of(e.getKey(), drivers.get(e.getKey().getDriverId()), e.getValue()))
                .toList();

        return PagedResponse.of(ranked, page, size);
    }

    private void applyEditableFields(RideOffer offer, OfferForm form) {
        offer.setPricePerSeat(form.pricePerSeat());
        offer.setNotes(form.notes());
        offer.setSmokingAllowed(form.smokingAllowed());
        offer.setPetsAllowed(form.petsAllowed());
    }

    private MatchCandidate candidate(RideOffer o) {
        return new MatchCandidate(o.getOrigin().getCityName(), o.getDestination().getCityName(),
                o.getDepartureDate(), o.getCreatedAt());
    }

    private RideOffer requireOffer(UUID offerId) {
        return offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
    }

    private void requireOwner(RideOffer offer, UUID driverId) {
        if (!offer.getDriverId().equals(driverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your offer");
        }
    }

    private UserSummary driverSummary(UUID driverId) {
        return userRepository.findById(driverId).map(UserSummary::from)
                .orElse(new UserSummary(driverId, null, null));
    }

    private Map<UUID, UserSummary> summaries(List<UUID> ids) {
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, UserSummary::from, (a, b) -> a));
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
