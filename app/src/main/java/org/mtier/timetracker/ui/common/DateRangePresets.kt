package org.mtier.timetracker.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** Mirrors the web app's dateRangePresets.js so both clients offer the same
 *  quick-range shortcuts. */
data class DateRangePreset(val key: String, val label: String)

val DATE_RANGE_PRESETS = listOf(
    DateRangePreset("today", "Today"),
    DateRangePreset("yesterday", "Yesterday"),
    DateRangePreset("last7", "Last 7 days"),
    DateRangePreset("last30", "Last 30 days"),
    DateRangePreset("last90", "Last 90 days"),
    DateRangePreset("last365", "Last 365 days"),
    DateRangePreset("thisMonth", "This month"),
    DateRangePreset("lastMonth", "Last month"),
    DateRangePreset("thisYear", "This year"),
    DateRangePreset("lastYear", "Last year"),
)

fun resolvePresetRange(key: String): Pair<Instant, Instant>? {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    fun startOfDay(date: LocalDate): Instant = date.atStartOfDay(zone).toInstant()
    fun endOfDay(date: LocalDate): Instant = date.plusDays(1).atStartOfDay(zone).toInstant().minusSeconds(1)

    return when (key) {
        "today" -> startOfDay(today) to endOfDay(today)
        "yesterday" -> today.minusDays(1).let { startOfDay(it) to endOfDay(it) }
        "last7" -> startOfDay(today.minusDays(6)) to endOfDay(today)
        "last30" -> startOfDay(today.minusDays(29)) to endOfDay(today)
        "last90" -> startOfDay(today.minusDays(89)) to endOfDay(today)
        "last365" -> startOfDay(today.minusDays(364)) to endOfDay(today)
        "thisMonth" -> startOfDay(YearMonth.from(today).atDay(1)) to endOfDay(today)
        "lastMonth" -> {
            val lastMonth = YearMonth.from(today).minusMonths(1)
            startOfDay(lastMonth.atDay(1)) to endOfDay(lastMonth.atEndOfMonth())
        }
        "thisYear" -> startOfDay(LocalDate.of(today.year, 1, 1)) to endOfDay(today)
        "lastYear" -> {
            val year = today.year - 1
            startOfDay(LocalDate.of(year, 1, 1)) to endOfDay(LocalDate.of(year, 12, 31))
        }
        else -> null
    }
}
