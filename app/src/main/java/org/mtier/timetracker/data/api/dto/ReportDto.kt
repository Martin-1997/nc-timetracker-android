package org.mtier.timetracker.data.api.dto

import kotlinx.serialization.Serializable

/** A single row from GET /ajax/report — shape depends on the requested
 *  group1/group2/timegroup, so most fields are nullable/best-effort. Note
 *  [time] is returned as a numeric string, not a JSON number. */
@Serializable
data class ReportItemDto(
    val id: Int? = null,
    val name: String? = null,
    val details: String? = null,
    val userUid: String? = null,
    val time: String? = null,
    val ftime: String? = null,
    val totalDuration: Long = 0,
    val project: String? = null,
    val client: String? = null,
    val cost: Int? = null,
)

@Serializable
data class ReportResponse(
    val items: List<ReportItemDto> = emptyList(),
    val total: Int = 0,
)
