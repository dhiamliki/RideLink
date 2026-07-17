package com.ridelink.proposal;

import com.ridelink.proposal.dto.CreateProposalRequest;
import com.ridelink.proposal.dto.ProposalResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @PostMapping("/requests/{requestId}/proposals")
    @ResponseStatus(HttpStatus.CREATED)
    public ProposalResponse propose(@AuthenticationPrincipal UUID userId, @PathVariable UUID requestId,
                                    @Valid @RequestBody(required = false) CreateProposalRequest body) {
        return proposalService.propose(userId, requestId, body);
    }

    @GetMapping("/requests/{requestId}/proposals")
    public List<ProposalResponse> forRequest(@AuthenticationPrincipal UUID userId, @PathVariable UUID requestId) {
        return proposalService.forRequest(userId, requestId);
    }

    @PostMapping("/proposals/{id}/withdraw")
    public ProposalResponse withdraw(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return proposalService.withdraw(userId, id);
    }

    @PostMapping("/proposals/{id}/accept")
    public ProposalResponse accept(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return proposalService.accept(userId, id);
    }

    @PostMapping("/proposals/{id}/decline")
    public ProposalResponse decline(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return proposalService.decline(userId, id);
    }

    @GetMapping("/proposals/mine")
    public List<ProposalResponse> mine(@AuthenticationPrincipal UUID userId) {
        return proposalService.mine(userId);
    }
}
