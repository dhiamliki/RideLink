package com.ridelink.chat;

import com.ridelink.booking.Booking;
import com.ridelink.booking.BookingRepository;
import com.ridelink.booking.BookingStatus;
import com.ridelink.chat.dto.ConversationResponse;
import com.ridelink.chat.dto.CounterpartDto;
import com.ridelink.chat.dto.MessageResponse;
import com.ridelink.chat.dto.PagedMessages;
import com.ridelink.chat.dto.ReadReceipt;
import com.ridelink.proposal.ProposalStatus;
import com.ridelink.proposal.RequestProposal;
import com.ridelink.proposal.RequestProposalRepository;
import com.ridelink.ride.RideOffer;
import com.ridelink.ride.RideOfferRepository;
import com.ridelink.ride.RideRequest;
import com.ridelink.ride.RideRequestRepository;
import com.ridelink.safety.SafetyService;
import com.ridelink.user.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// Chat's eligibility rule (the critical bit): a conversation between two users may exist AND be used
// only while there is an ACCEPTED booking or ACCEPTED proposal connecting them, and neither has
// blocked the other. Enforced on get-or-create AND on every message send/read.
@Service
public class ChatService {

    private static final int MAX_CONTENT = 2000;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final RequestProposalRepository proposalRepository;
    private final RideOfferRepository offerRepository;
    private final RideRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final SafetyService safetyService;

    public ChatService(ConversationRepository conversationRepository, MessageRepository messageRepository,
                       BookingRepository bookingRepository, RequestProposalRepository proposalRepository,
                       RideOfferRepository offerRepository, RideRequestRepository requestRepository,
                       UserRepository userRepository, SafetyService safetyService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.bookingRepository = bookingRepository;
        this.proposalRepository = proposalRepository;
        this.offerRepository = offerRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.safetyService = safetyService;
    }

    // --- Get-or-create (verifies acceptance + that the caller is a party to the handshake) ---

    @Transactional
    public ConversationResponse fromBooking(UUID me, UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        RideOffer offer = offerRepository.findById(booking.getOfferId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        UUID passenger = booking.getPassengerId();
        UUID driver = offer.getDriverId();
        if (!me.equals(passenger) && !me.equals(driver)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chat unlocks only once the booking is accepted");
        }
        safetyService.assertNotBlocked(passenger, driver);
        Conversation c = getOrCreate(passenger, driver, bookingId, null);
        return toResponse(c, me);
    }

    @Transactional
    public ConversationResponse fromProposal(UUID me, UUID proposalId) {
        RequestProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found"));
        RideRequest request = requestRepository.findById(proposal.getRequestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        UUID driver = proposal.getDriverId();
        UUID passenger = request.getPassengerId();
        if (!me.equals(driver) && !me.equals(passenger)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your proposal");
        }
        if (proposal.getStatus() != ProposalStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chat unlocks only once the proposal is accepted");
        }
        safetyService.assertNotBlocked(driver, passenger);
        Conversation c = getOrCreate(driver, passenger, null, proposalId);
        return toResponse(c, me);
    }

    // --- Read paths ---

    @Transactional(readOnly = true)
    public List<ConversationResponse> listMine(UUID me) {
        List<ConversationResponse> out = new ArrayList<>();
        for (Conversation c : conversationRepository.findMine(me)) {
            UUID counterpart = c.counterpartOf(me);
            // Defensive: hide a conversation with someone now blocked in either direction.
            if (safetyService.isBlockedBetween(me, counterpart)) {
                continue;
            }
            out.add(toResponse(c, me));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public PagedMessages messages(UUID me, UUID conversationId, int page, int size) {
        Conversation c = requireParticipant(me, conversationId);
        Pageable pageable = PageRequest.of(Math.max(page, 0), size <= 0 ? 30 : Math.min(size, 100));
        return PagedMessages.of(
                messageRepository.findByConversationIdOrderBySentAtDesc(c.getId(), pageable)
                        .map(MessageResponse::of));
    }

    // --- Write paths (called from both REST-less STOMP and, in future, anywhere) ---

    @Transactional
    public MessageResponse sendMessage(UUID me, UUID conversationId, String rawContent) {
        Conversation c = requireParticipant(me, conversationId);
        String content = rawContent == null ? "" : rawContent.trim();
        if (content.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty");
        }
        if (content.length() > MAX_CONTENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is too long");
        }
        assertEligible(me, c.counterpartOf(me));
        Message saved = messageRepository.save(new Message(c.getId(), me, content));
        return MessageResponse.of(saved);
    }

    @Transactional
    public ReadReceipt markRead(UUID me, UUID conversationId) {
        Conversation c = requireParticipant(me, conversationId);
        Instant now = Instant.now();
        int count = messageRepository.markRead(c.getId(), me, now);
        return new ReadReceipt(c.getId(), me, now, count);
    }

    // --- Helpers ---

    private Conversation getOrCreate(UUID u1, UUID u2, UUID bookingId, UUID proposalId) {
        String key = Conversation.pairKey(u1, u2);
        return conversationRepository.findByPairKey(key).orElseGet(() -> {
            try {
                return conversationRepository.saveAndFlush(Conversation.between(u1, u2, bookingId, proposalId));
            } catch (DataIntegrityViolationException race) {
                // Concurrent create won the unique index; use theirs.
                return conversationRepository.findByPairKey(key)
                        .orElseThrow(() -> race);
            }
        });
    }

    private Conversation requireParticipant(UUID me, UUID conversationId) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        if (!c.hasParticipant(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant");
        }
        return c;
    }

    // The chat gate: there must be a live ACCEPTED connection AND no block between the two users.
    private void assertEligible(UUID a, UUID b) {
        boolean connected = !bookingRepository.findByStatusBetween(a, b, BookingStatus.ACCEPTED).isEmpty()
                || !proposalRepository.findByStatusBetween(a, b, ProposalStatus.ACCEPTED).isEmpty();
        if (!connected) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No accepted connection with this user");
        }
        safetyService.assertNotBlocked(a, b);
    }

    private ConversationResponse toResponse(Conversation c, UUID me) {
        CounterpartDto counterpart = userRepository.findById(c.counterpartOf(me))
                .map(CounterpartDto::of)
                .orElse(null);
        MessageResponse last = messageRepository.findFirstByConversationIdOrderBySentAtDesc(c.getId())
                .map(MessageResponse::of)
                .orElse(null);
        long unread = messageRepository.countByConversationIdAndSenderIdNotAndReadAtIsNull(c.getId(), me);
        return new ConversationResponse(c.getId(), counterpart, last, unread, c.getCreatedAt());
    }
}
