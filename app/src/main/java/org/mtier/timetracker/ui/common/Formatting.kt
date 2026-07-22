package org.mtier.timetracker.ui.common

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60
private const val CENTS_PER_UNIT = 100.0

/** HH:MM:SS, matching the web frontend's Timer/Reports/Timelines duration format. */
fun formatDurationHms(totalSeconds: Long): String {
    val seconds = totalSeconds.coerceAtLeast(0)
    val h = seconds / SECONDS_PER_HOUR
    val m = (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val s = seconds % SECONDS_PER_MINUTE
    return "%02d:%02d:%02d".format(h, m, s)
}

fun formatCents(cents: Int): String = "%.2f".format(cents / CENTS_PER_UNIT)
