package org.mtier.timetracker.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditNameRequest(
    val name: String,
)

@Serializable
data class AddProjectRequest(
    val clientId: String = "",
    val color: String = "",
)

@Serializable
data class EditProjectRequest(
    val name: String,
    val clientId: String = "",
    val color: String = "",
    val locked: String = "0",
    val archived: String = "0",
    val allowedTags: String = "",
    val allowedUsers: String = "",
)

@Serializable
data class StartTimerRequest(
    val projectId: String = "",
    val tags: String = "",
)

@Serializable
data class UpdateNameDetailsRequest(
    val name: String? = null,
    val details: String? = null,
)

@Serializable
data class UpdateProjectRequest(
    val projectId: String = "",
)

@Serializable
data class UpdateTagsRequest(
    val tagId: String,
)

@Serializable
data class UpdateTimeRequest(
    val start: String,
    val end: String,
    val tzoffset: Int,
)

@Serializable
data class AddWorkIntervalRequest(
    val start: String,
    val end: String,
    val tzoffset: Int,
    val details: String = "",
)

@Serializable
data class AddCostRequest(
    val cost: String,
)
