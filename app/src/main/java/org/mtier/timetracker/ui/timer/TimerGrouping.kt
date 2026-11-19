package org.mtier.timetracker.ui.timer

import org.mtier.timetracker.data.api.dto.DayGroupDto
import org.mtier.timetracker.data.api.dto.WorkIntervalItemDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Entries of one day with the same name, as the server already groups them
 * in `work-intervals` (`days[day][name]`). Same as the web app's Timer view
 * (mtierltd/timetracker#299): a group with more than one entry gets a group
 * row with the entry count, subtotal and a resume button.
 */
data class NameGroup(
    /** Stable across refreshes; used to remember folded groups. */
    val key: String,
    val name: String,
    /** Newest first. */
    val entries: List<WorkIntervalItemDto>,
) {
    val newest: WorkIntervalItemDto get() = entries.first()
    val isRunning: Boolean get() = entries.any { it.isRunning }

    /** Stopped entries only; add the live time of a running one on top. */
    val stoppedSeconds: Long get() = entries.sumOf { (it.duration ?: 0).toLong() }
}

data class DayGroup(
    val label: String,
    /** Ordered by their newest entry, newest first. */
    val groups: List<NameGroup>,
) {
    val isRunning: Boolean get() = groups.any { it.isRunning }

    /** Stopped entries only; add the live time of a running one on top. */
    val stoppedSeconds: Long get() = groups.sumOf { it.stoppedSeconds }
}

val WorkIntervalItemDto.isRunning: Boolean get() = running == 1 || duration == null

fun buildDayGroups(days: Map<String, Map<String, DayGroupDto>>): List<DayGroup> =
    days.map { (day, byName) ->
        DayGroup(
            label = day,
            groups =
                byName
                    .filterValues { it.children.isNotEmpty() }
                    .map { (name, group) ->
                        NameGroup(
                            key = "$day\u0000$name",
                            name = group.children.first().name,
                            entries = group.children.sortedByDescending { it.start },
                        )
                    }.sortedByDescending { it.newest.start },
        )
    }

private val CLOCK_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** "HH:MM - HH:MM" in local time, "HH:MM -" while running (as on the web, #307). */
fun formatStartEnd(
    item: WorkIntervalItemDto,
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val start = CLOCK_FORMATTER.withZone(zone).format(Instant.ofEpochSecond(item.start))
    if (item.isRunning) return "$start -"
    val end = CLOCK_FORMATTER.withZone(zone).format(Instant.ofEpochSecond(item.start + (item.duration ?: 0)))
    return "$start - $end"
}
