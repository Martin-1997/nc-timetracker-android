package org.mtier.timetracker.data.repository

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

/**
 * The backend parses start/end with PHP's DateTime::createFromFormat
 * ("d/m/y H:i", ...) in UTC, then applies tzoffset*60 seconds itself — the
 * exact same contract the Vue frontend's manual-entry/edit-time forms use.
 */
private val API_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")

fun Instant.toApiDateString(): String =
    API_DATE_FORMATTER.withZone(ZoneId.systemDefault()).format(this)

/** Java's getRawOffset is milliseconds east of UTC; the PHP side expects
 *  JS's getTimezoneOffset() convention (minutes *west* of UTC), so negate. */
fun currentTzOffsetMinutes(): Int =
    -(TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60_000)
