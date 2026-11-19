package org.mtier.timetracker.data.repository

/**
 * Server features the app may need to check, keyed by the stable identifier
 * the server publishes in its `timetracker.features` capability
 * (mtierltd/timetracker#312, documented in the server repo's FEATURES.md).
 *
 * [legacy] features existed before the server published capabilities, so a
 * server without the `timetracker` block still has them. Every feature added
 * after that is shown only when the server lists it.
 */
enum class ServerFeature(
    val id: String,
    val legacy: Boolean,
) {
    GOALS("goals", legacy = true),
    TIMELINES("timelines", legacy = true),
    TIMER_GROUPING("timer_grouping", legacy = true),
    PINNED_TIMERS("pinned_timers", legacy = false),
}

/**
 * What the connected server supports. [UNKNOWN] is used before the first
 * successful fetch and for servers without the `timetracker` block: it keeps
 * the [ServerFeature.legacy] features and hides everything newer.
 */
data class TimeTrackerCapabilities(
    val published: Boolean,
    val version: String? = null,
    val apiVersions: List<String> = emptyList(),
    val features: Set<String> = emptySet(),
) {
    fun supports(feature: ServerFeature): Boolean = if (published) feature.id in features else feature.legacy

    companion object {
        val UNKNOWN = TimeTrackerCapabilities(published = false)
    }
}
