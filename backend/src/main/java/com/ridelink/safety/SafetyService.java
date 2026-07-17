package com.ridelink.safety;

import com.ridelink.booking.Booking;
import com.ridelink.booking.BookingRepository;
import com.ridelink.booking.BookingStatus;
import com.ridelink.proposal.ProposalStatus;
import com.ridelink.proposal.RequestProposal;
import com.ridelink.proposal.RequestProposalRepository;
import com.ridelink.safety.dto.BlockedUserResponse;
import com.ridelink.safety.dto.CreateBlockRequest;
import com.ridelink.safety.dto.CreateReportRequest;
import com.ridelink.safety.dto.ReportResponse;
import com.ridelink.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// Central home for report + block. The block checks (assertNotBlocked / isBlockedBetween /
// blockedUserIdsFor) are the single source of truth reused by booking, proposal, and browse —
// enforcement logic lives here, not scattered across those services.
@Service
public class SafetyService {

    private final ReportRepository reportRepository;
    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final RequestProposalRepository proposalRepository;

    public SafetyService(ReportRepository reportRepository, BlockRepository blockRepository,
                         UserRepository userRepository, BookingRepository bookingRepository,
                         RequestProposalRepository proposalRepository) {
        this.reportRepository = reportRepository;
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.proposalRepository = proposalRepository;
    }

    // --- Reusable enforcement helpers ---

    // True if either user has blocked the other (a block protects in both directions).
    @Transactional(readOnly = true)
    public boolean isBlockedBetween(UUID u1, UUID u2) {
        return blockRepository.existsBetween(u1, u2);
    }

    // Throws 403 if the two users are blocked in either direction; used before booking/proposing.
    @Transactional(readOnly = true)
    public void assertNotBlocked(UUID u1, UUID u2) {
        if (blockRepository.existsBetween(u1, u2)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This user is unavailable");
        }
    }

    // Ids to hide from `me` in browse/search (everyone `me` blocked + everyone who blocked `me`).
    @Transactional(readOnly = true)
    public Set<UUID> blockedUserIdsFor(UUID me) {
        return Set.copyOf(blockRepository.findCounterpartIds(me));
    }

    // --- Report ---

    @Transactional
    public ReportResponse report(UUID reporterId, CreateReportRequest body) {
        if (reporterId.equals(body.reportedUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot report yourself");
        }
        requireUser(body.reportedUserId());
        // Flush so the @CreationTimestamp is populated on the returned entity.
        Report saved = reportRepository.saveAndFlush(
                new Report(reporterId, body.reportedUserId(), body.reason(), body.detail()));
        return ReportResponse.of(saved);
    }

    // --- Block ---

    // Idempotent: blocking an already-blocked user is a no-op. On a new block we also auto-decline
    // any still-pending booking/proposal between the pair so the block can't be bypassed by an
    // in-flight handshake accepted after the block.
    @Transactional
    public void block(UUID blockerId, CreateBlockRequest body) {
        UUID blockedUserId = body.blockedUserId();
        if (blockerId.equals(blockedUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot block yourself");
        }
        requireUser(blockedUserId);
        if (blockRepository.existsByBlockerIdAndBlockedUserId(blockerId, blockedUserId)) {
            return;
        }
        blockRepository.save(new Block(blockerId, blockedUserId));
        autoDeclinePending(blockerId, blockedUserId);
    }

    @Transactional
    public void unblock(UUID blockerId, UUID blockedUserId) {
        blockRepository.findByBlockerIdAndBlockedUserId(blockerId, blockedUserId)
                .ifPresent(blockRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<BlockedUserResponse> listBlocked(UUID blockerId) {
        List<UUID> ids = blockRepository.findByBlockerIdOrderByCreatedAtDesc(blockerId).stream()
                .map(Block::getBlockedUserId)
                .toList();
        return userRepository.findAllById(ids).stream()
                .map(BlockedUserResponse::from)
                .toList();
    }

    private void autoDeclinePending(UUID u1, UUID u2) {
        Instant now = Instant.now();
        List<Booking> bookings = bookingRepository.findByStatusBetween(u1, u2, BookingStatus.REQUESTED);
        for (Booking b : bookings) {
            b.setStatus(BookingStatus.DECLINED);
            b.setDecidedAt(now);
        }
        bookingRepository.saveAll(bookings);

        List<RequestProposal> proposals = proposalRepository.findByStatusBetween(u1, u2, ProposalStatus.PROPOSED);
        for (RequestProposal p : proposals) {
            p.setStatus(ProposalStatus.DECLINED);
            p.setDecidedAt(now);
        }
        proposalRepository.saveAll(proposals);
    }

    private void requireUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
