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

private const val MILLIS_PER_MINUTE = 60_000

fun Instant.toApiDateString(): String = API_DATE_FORMATTER.withZone(ZoneId.systemDefault()).format(this)

/** Java's getOffset is milliseconds east of UTC; the PHP side expects
 *  JS's getTimezoneOffset() convention (minutes *west* of UTC), so negate. */
fun currentTzOffsetMinutes(): Int = tzOffsetMinutesAt(Instant.ofEpochMilli(System.currentTimeMillis()))

/**
 * The offset in effect at [instant], not today's: a time on the other side
 * of a daylight saving change would otherwise be stored an hour off
 * (mtierltd/timetracker#151). Sent per time as `tzoffset` / `tzoffsetEnd`.
 */
fun tzOffsetMinutesAt(
    instant: Instant,
    timeZone: TimeZone = TimeZone.getDefault(),
): Int = -(timeZone.getOffset(instant.toEpochMilli()) / MILLIS_PER_MINUTE)
