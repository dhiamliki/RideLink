package com.ridelink.safety.dto;

import com.ridelink.safety.Report;
import com.ridelink.safety.ReportReason;
import com.ridelink.safety.ReportStatus;
import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        UUID reportedUserId,
        ReportReason reason,
        ReportStatus status,
        Instant createdAt) {

    public static ReportResponse of(Report r) {
        return new ReportResponse(r.getId(), r.getReportedUserId(), r.getReason(), r.getStatus(), r.getCreatedAt());
    }
}
