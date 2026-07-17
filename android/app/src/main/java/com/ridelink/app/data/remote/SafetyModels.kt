package com.ridelink.app.data.remote

// --- Safety: report + block (Task 3b) ---

// reason is one of the backend enum values (see ReportReasons in ui/common). detail is optional.
data class CreateReportBody(val reportedUserId: String, val reason: String, val detail: String?)

data class CreateBlockBody(val blockedUserId: String)

// A user the current user has blocked (GET /api/blocks).
data class BlockedUser(
    val id: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
)
