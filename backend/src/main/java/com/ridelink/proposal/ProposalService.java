package com.ridelink.proposal;

import com.ridelink.proposal.dto.ContactDto;
import com.ridelink.proposal.dto.CreateProposalRequest;
import com.ridelink.proposal.dto.ProposalResponse;
import com.ridelink.proposal.dto.RequestSummary;
import com.ridelink.ride.RideRequest;
import com.ridelink.ride.RideRequestRepository;
import com.ridelink.ride.RideRequestStatus;
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
public class ProposalService {

    // A driver may hold at most one live proposal per request.
    private static final List<ProposalStatus> BLOCKING_STATUSES =
            List.of(ProposalStatus.PROPOSED, ProposalStatus.ACCEPTED);

    private final RequestProposalRepository proposalRepository;
    private final RideRequestRepository requestRepository;
    private final UserRepository userRepository;

    public ProposalService(RequestProposalRepository proposalRepository,
                           RideRequestRepository requestRepository, UserRepository userRepository) {
        this.proposalRepository = proposalRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProposalResponse propose(UUID driverId, UUID requestId, CreateProposalRequest body) {
        RideRequest request = requireRequest(requestId);
        if (request.getPassengerId().equals(driverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot propose on your own request");
        }
        if (request.getStatus() != RideRequestStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request is not active");
        }
        if (proposalRepository.existsByRequestIdAndDriverIdAndStatusIn(requestId, driverId, BLOCKING_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have an active proposal on this request");
        }
        String message = body == null ? null : body.message();
        RequestProposal saved = proposalRepository.save(
                new RequestProposal(requestId, driverId, message, body == null ? null : body.pricePerSeat()));
        return toResponse(saved, request, driverId);
    }

    @Transactional
    public ProposalResponse withdraw(UUID driverId, UUID proposalId) {
        RequestProposal proposal = requireProposal(proposalId);
        if (!proposal.getDriverId().equals(driverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your proposal");
        }
        requireStatus(proposal, ProposalStatus.PROPOSED);
        proposal.setStatus(ProposalStatus.WITHDRAWN);
        proposal.setDecidedAt(Instant.now());
        RequestProposal saved = proposalRepository.save(proposal);
        return toResponse(saved, requestRepository.findById(proposal.getRequestId()).orElse(null), driverId);
    }

    @Transactional
    public ProposalResponse accept(UUID passengerId, UUID proposalId) {
        RequestProposal proposal = requireProposal(proposalId);
        RideRequest request = requireRequest(proposal.getRequestId());
        requireOwner(request, passengerId);
        requireStatus(proposal, ProposalStatus.PROPOSED);

        Instant now = Instant.now();
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposal.setDecidedAt(now);

        // Request is now spoken for: it drops out of the ACTIVE browse list...
        request.setStatus(RideRequestStatus.FULFILLED);
        requestRepository.save(request);

        // ...and every other still-pending proposal on it is auto-declined. Same tx -> atomic.
        List<RequestProposal> others = proposalRepository.findByRequestIdAndStatus(
                proposal.getRequestId(), ProposalStatus.PROPOSED);
        for (RequestProposal other : others) {
            if (!other.getId().equals(proposal.getId())) {
                other.setStatus(ProposalStatus.DECLINED);
                other.setDecidedAt(now);
            }
        }
        proposalRepository.saveAll(others);
        RequestProposal saved = proposalRepository.save(proposal);
        return toResponse(saved, request, passengerId);
    }

    @Transactional
    public ProposalResponse decline(UUID passengerId, UUID proposalId) {
        RequestProposal proposal = requireProposal(proposalId);
        RideRequest request = requireRequest(proposal.getRequestId());
        requireOwner(request, passengerId);
        requireStatus(proposal, ProposalStatus.PROPOSED);

        proposal.setStatus(ProposalStatus.DECLINED);
        proposal.setDecidedAt(Instant.now());
        return toResponse(proposalRepository.save(proposal), request, passengerId);
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> mine(UUID driverId) {
        List<RequestProposal> proposals = proposalRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        Map<UUID, RideRequest> requests = requestsById(proposals.stream().map(RequestProposal::getRequestId).toList());
        return proposals.stream()
                .map(p -> toResponse(p, requests.get(p.getRequestId()), driverId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> forRequest(UUID ownerId, UUID requestId) {
        RideRequest request = requireRequest(requestId);
        requireOwner(request, ownerId);
        return proposalRepository.findByRequestIdOrderByCreatedAtDesc(requestId).stream()
                .map(p -> toResponse(p, request, ownerId))
                .toList();
    }

    // Reveals the counterpart's contact (driver <-> passenger) only on ACCEPTED proposals.
    private ProposalResponse toResponse(RequestProposal proposal, RideRequest request, UUID viewerId) {
        ContactDto contact = null;
        if (proposal.getStatus() == ProposalStatus.ACCEPTED && request != null) {
            UUID counterpartId = viewerId.equals(proposal.getDriverId())
                    ? request.getPassengerId()
                    : proposal.getDriverId();
            contact = userRepository.findById(counterpartId)
                    .map(u -> new ContactDto(u.getDisplayName(), u.getPhoneNumber()))
                    .orElse(null);
        }
        RequestSummary summary = request == null ? null : RequestSummary.from(request);
        return ProposalResponse.of(proposal, summary, contact);
    }

    private Map<UUID, RideRequest> requestsById(List<UUID> ids) {
        return requestRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(RideRequest::getId, Function.identity(), (a, b) -> a));
    }

    private RequestProposal requireProposal(UUID id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));
    }

    private RideRequest requireRequest(UUID id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
    }

    private void requireOwner(RideRequest request, UUID passengerId) {
        if (!request.getPassengerId().equals(passengerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        }
    }

    private void requireStatus(RequestProposal proposal, ProposalStatus expected) {
        if (proposal.getStatus() != expected) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposal is not " + expected);
        }
    }
}
