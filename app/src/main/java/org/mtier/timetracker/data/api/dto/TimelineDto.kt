package org.mtier.timetracker.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A saved timeline export — [totalDuration] is a numeric string here
 *  (Timeline entity's own `addType`), unlike ReportItemDto's totalDuration. */
@Serializable
data class TimelineDto(
    val id: Int,
    val status: String = "pending",
    val userUid: String? = null,
    val timeInterval: String? = null,
    val totalDuration: String = "0",
    val createdAt: Long = 0,
)

@Serializable
data class TimelinesResponse(
    @SerialName("Timelines") val timelines: List<TimelineDto> = emptyList(),
    val total: Int = 0,
)
