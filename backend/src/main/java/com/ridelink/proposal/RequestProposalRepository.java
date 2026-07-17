package com.ridelink.proposal;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestProposalRepository extends JpaRepository<RequestProposal, UUID> {

    List<RequestProposal> findByDriverIdOrderByCreatedAtDesc(UUID driverId);

    List<RequestProposal> findByRequestIdOrderByCreatedAtDesc(UUID requestId);

    List<RequestProposal> findByRequestIdAndStatus(UUID requestId, ProposalStatus status);

    boolean existsByRequestIdAndDriverIdAndStatusIn(UUID requestId, UUID driverId,
                                                    Collection<ProposalStatus> statuses);
}
