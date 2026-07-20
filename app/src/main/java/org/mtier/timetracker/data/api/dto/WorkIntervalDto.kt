package org.mtier.timetracker.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A single tracked interval as returned inside a day group's `children`. */
@Serializable
data class WorkIntervalItemDto(
    val id: Int,
    val name: String,
    val details: String? = null,
    val projectId: Int? = null,
    val running: Int = 0,
    val start: Long,
    // null while the interval is still running — the backend only knows the
    // final duration once it's stopped.
    val duration: Int? = null,
    val tags: List<TagDto> = emptyList(),
    val userUid: String? = null,
    val cost: Int? = null,
    val projectName: String? = null,
    val projectColor: String? = null,
)

@Serializable
data class DayGroupDto(
    val children: List<WorkIntervalItemDto> = emptyList(),
    val totalTime: Int = 0,
)

/** Shape used for the top-level `running` array (no tags/project name joined in). */
@Serializable
data class RunningIntervalDto(
    val id: Int,
    val name: String,
    val details: String? = null,
    val projectId: Int? = null,
    val userUid: String? = null,
    val start: Long,
    // null while the interval is still running — see WorkIntervalItemDto.duration.
    val duration: Int? = null,
    val running: Int = 0,
    val cost: Int? = null,
)

@Serializable
data class WorkIntervalsResponse(
    val running: List<RunningIntervalDto> = emptyList(),
    val days: Map<String, Map<String, DayGroupDto>> = emptyMap(),
    val now: Long = 0,
    @SerialName("WorkIntervals") val workIntervals: List<RunningIntervalDto> = emptyList(),
)
