package com.ridelink.proposal;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RequestProposalRepository extends JpaRepository<RequestProposal, UUID> {

    List<RequestProposal> findByDriverIdOrderByCreatedAtDesc(UUID driverId);

    List<RequestProposal> findByRequestIdOrderByCreatedAtDesc(UUID requestId);

    List<RequestProposal> findByRequestIdAndStatus(UUID requestId, ProposalStatus status);

    boolean existsByRequestIdAndDriverIdAndStatusIn(UUID requestId, UUID driverId,
                                                    Collection<ProposalStatus> statuses);

    // [requestId, count] of still-pending (PROPOSED) proposals per request, for the owner's My Rides
    // badge. One grouped query over a page of requests instead of N per-request calls.
    @Query("select p.requestId, count(p) from RequestProposal p "
            + "where p.requestId in :requestIds and p.status = com.ridelink.proposal.ProposalStatus.PROPOSED "
            + "group by p.requestId")
    List<Object[]> countPendingByRequestIds(@Param("requestIds") Collection<UUID> requestIds);

    // Proposals in the given status linking the two users as driver<->request-owner (either
    // direction). Used to auto-decline pending proposals when a block is created (SafetyService).
    @Query("select p from RequestProposal p, RideRequest r where p.requestId = r.id and p.status = :status "
            + "and ((p.driverId = :u1 and r.passengerId = :u2) "
            + "or (p.driverId = :u2 and r.passengerId = :u1))")
    List<RequestProposal> findByStatusBetween(@Param("u1") UUID u1, @Param("u2") UUID u2,
                                              @Param("status") ProposalStatus status);
}
