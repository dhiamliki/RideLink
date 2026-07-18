package com.ridelink.app.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Human-friendly rendering of the backend's LocalDate / LocalTime strings.
// All helpers are null-safe and fall back to the raw value if parsing fails,
// so a malformed payload can never crash a screen.

private val DAY_MONTH = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
private val DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val TIME_HHMM = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

// "2026-11-01" -> "1 Nov" (or "1 Nov 2026" when not the current year).
fun formatRideDate(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        val date = LocalDate.parse(raw.trim())
        val formatter = if (date.year == LocalDate.now().year) DAY_MONTH else DAY_MONTH_YEAR
        date.format(formatter)
    } catch (e: Exception) {
        raw
    }
}

// "09:00:00" -> "09:00".
fun formatRideTime(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        LocalTime.parse(raw.trim()).format(TIME_HHMM)
    } catch (e: Exception) {
        raw
    }
}

// Combines a date and a time/time-window into "1 Nov · 09:00", omitting missing parts.
fun formatDateTime(date: String?, time: String?): String =
    listOfNotNull(formatRideDate(date), formatRideTime(time))
        .filter { it.isNotBlank() }
        .joinToString(" · ")

// An ISO-8601 instant (e.g. a message's sentAt "2026-07-18T14:37:07Z") -> local "14:37".
fun formatTimestamp(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        Instant.parse(raw.trim()).atZone(ZoneId.systemDefault()).format(TIME_HHMM)
    } catch (e: Exception) {
        ""
    }
}
