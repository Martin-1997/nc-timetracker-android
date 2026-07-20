package org.mtier.timetracker.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** GET /ajax/goals row — hours/debt/remaining fields are server-computed
 *  (PHP round()), hence Double rather than Int. */
@Serializable
data class GoalDto(
    val id: Int,
    val userUid: String? = null,
    val projectId: Int? = null,
    val projectName: String? = null,
    val hours: Int = 0,
    val interval: String = "",
    val createdAt: Long = 0,
    val workedHoursCurrentPeriod: Double = 0.0,
    val debtHours: Double = 0.0,
    val remainingHours: Double = 0.0,
    val totalRemainingHours: Double = 0.0,
)

@Serializable
data class GoalsResponse(
    @SerialName("Goals") val goals: List<GoalDto> = emptyList(),
)
