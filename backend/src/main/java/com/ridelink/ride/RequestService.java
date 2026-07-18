package com.ridelink.ride;

import com.ridelink.proposal.RequestProposalRepository;
import com.ridelink.ride.dto.MyRequestResponse;
import com.ridelink.ride.dto.PagedResponse;
import com.ridelink.ride.dto.RequestForm;
import com.ridelink.ride.dto.RequestResponse;
import com.ridelink.ride.dto.UserSummary;
import com.ridelink.ride.match.MatchCandidate;
import com.ridelink.ride.match.MatchQuery;
import com.ridelink.ride.match.MatchingStrategy;
import com.ridelink.safety.SafetyService;
import com.ridelink.user.User;
import com.ridelink.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
public class RequestService {

    private final RideRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final MatchingStrategy matchingStrategy;
    private final SafetyService safetyService;
    private final RequestProposalRepository proposalRepository;

    public RequestService(RideRequestRepository requestRepository, UserRepository userRepository,
                          MatchingStrategy matchingStrategy, SafetyService safetyService,
                          RequestProposalRepository proposalRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.matchingStrategy = matchingStrategy;
        this.safetyService = safetyService;
        this.proposalRepository = proposalRepository;
    }

    @Transactional
    public RequestResponse create(UUID passengerId, RequestForm form) {
        RideRequest request = new RideRequest(passengerId, form.origin().toLocation(),
                form.destination().toLocation(), form.preferredDate(), form.preferredTimeWindow(),
                form.seatsNeededOrDefault());
        request.setMaxPricePerSeat(form.maxPricePerSeat());
        request.setNotes(form.notes());
        RideRequest saved = requestRepository.save(request);
        return RequestResponse.of(saved, passengerSummary(passengerId), null);
    }

    @Transactional
    public RequestResponse update(UUID passengerId, UUID requestId, RequestForm form) {
        RideRequest request = requireRequest(requestId);
        requireOwner(request, passengerId);
        if (request.getStatus() != RideRequestStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only active requests can be edited");
        }
        request.setOrigin(form.origin().toLocation());
        request.setDestination(form.destination().toLocation());
        request.setPreferredDate(form.preferredDate());
        request.setPreferredTimeWindow(form.preferredTimeWindow());
        request.setSeatsNeeded(form.seatsNeededOrDefault());
        request.setMaxPricePerSeat(form.maxPricePerSeat());
        request.setNotes(form.notes());
        return RequestResponse.of(request, passengerSummary(passengerId), null);
    }

    @Transactional
    public void cancel(UUID passengerId, UUID requestId) {
        RideRequest request = requireRequest(requestId);
        requireOwner(request, passengerId);
        if (request.getStatus() != RideRequestStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only active requests can be cancelled");
        }
        request.setStatus(RideRequestStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public RequestResponse get(UUID viewerId, UUID requestId) {
        RideRequest request = requireRequest(requestId);
        // Hide a blocked user's request from existence.
        if (safetyService.isBlockedBetween(viewerId, request.getPassengerId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found");
        }
        return RequestResponse.of(request, passengerSummary(request.getPassengerId()), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RequestResponse> search(UUID viewerId, String originCity, String destCity, LocalDate date,
                                                 Integer minSeats, int page, int size) {
        Specification<RideRequest> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("status"), RideRequestStatus.ACTIVE));
            if (hasText(originCity)) {
                ps.add(cb.equal(cb.lower(root.get("origin").<String>get("cityName")), originCity.trim().toLowerCase()));
            }
            if (hasText(destCity)) {
                ps.add(cb.equal(cb.lower(root.get("destination").<String>get("cityName")), destCity.trim().toLowerCase()));
            }
            if (date != null) {
                ps.add(cb.equal(root.get("preferredDate"), date));
            }
            if (minSeats != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<Integer>get("seatsNeeded"), minSeats));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Set<UUID> blocked = safetyService.blockedUserIdsFor(viewerId);
        List<RideRequest> found = requestRepository.findAll(spec).stream()
                .filter(r -> !blocked.contains(r.getPassengerId()))
                .toList();
        MatchQuery query = new MatchQuery(originCity, destCity, date);
        Map<UUID, UserSummary> passengers = summaries(found.stream().map(RideRequest::getPassengerId).toList());

        List<RequestResponse> ranked = found.stream()
                .map(r -> Map.entry(r, matchingStrategy.score(query, candidate(r))))
                .sorted(Comparator.<Map.Entry<RideRequest, Integer>>comparingInt(Map.Entry::getValue).reversed()
                        .thenComparing(e -> e.getKey().getCreatedAt(), Comparator.reverseOrder()))
                .map(e -> RequestResponse.of(e.getKey(), passengers.get(e.getKey().getPassengerId()), e.getValue()))
                .toList();

        return PagedResponse.of(ranked, page, size);
    }

    // The owner's own listings: ALL of their requests regardless of status (no browse filtering),
    // newest first, each carrying its count of still-pending proposals.
    @Transactional(readOnly = true)
    public PagedResponse<MyRequestResponse> mine(UUID passengerId, int page, int size) {
        List<RideRequest> requests = requestRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
        Map<UUID, Long> pending = pendingCounts(requests);
        UserSummary passenger = passengerSummary(passengerId);
        List<MyRequestResponse> rows = requests.stream()
                .map(r -> MyRequestResponse.of(r, passenger, pending.getOrDefault(r.getId(), 0L).intValue()))
                .toList();
        return PagedResponse.of(rows, page, size);
    }

    private Map<UUID, Long> pendingCounts(List<RideRequest> requests) {
        if (requests.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = requests.stream().map(RideRequest::getId).toList();
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : proposalRepository.countPendingByRequestIds(ids)) {
            counts.put((UUID) row[0], (Long) row[1]);
        }
        return counts;
    }

    private MatchCandidate candidate(RideRequest r) {
        return new MatchCandidate(r.getOrigin().getCityName(), r.getDestination().getCityName(),
                r.getPreferredDate(), r.getCreatedAt());
    }

    private RideRequest requireRequest(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
    }

    private void requireOwner(RideRequest request, UUID passengerId) {
        if (!request.getPassengerId().equals(passengerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        }
    }

    private UserSummary passengerSummary(UUID passengerId) {
        return userRepository.findById(passengerId).map(UserSummary::from)
                .orElse(new UserSummary(passengerId, null, null));
    }

    private Map<UUID, UserSummary> summaries(List<UUID> ids) {
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, UserSummary::from, (a, b) -> a));
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
