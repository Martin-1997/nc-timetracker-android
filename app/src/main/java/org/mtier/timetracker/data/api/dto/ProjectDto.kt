package org.mtier.timetracker.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Basic project shape from GET /ajax/projects — used for pickers. */
@Serializable
data class ProjectDto(
    val id: Int,
    val name: String,
    val color: String? = null,
    val clientId: Int? = null,
    val locked: Int? = null,
    val archived: Int? = null,
    val createdAt: Long? = null,
)

@Serializable
data class ProjectsResponse(
    @SerialName("Projects") val projects: List<ProjectDto> = emptyList(),
)

/** Full row shape from GET /ajax/projects-table — used by the Projects CRUD screen. */
@Serializable
data class ProjectTableRowDto(
    val id: Int,
    val name: String,
    val color: String? = null,
    val locked: Int = 0,
    val archived: Int = 0,
    val client: String? = null,
    val clientId: Int? = null,
    val allowedTags: List<TagDto> = emptyList(),
    val allowedUsers: List<String> = emptyList(),
)

@Serializable
data class ProjectsTableResponse(
    val items: List<ProjectTableRowDto> = emptyList(),
    val total: Int = 0,
)
