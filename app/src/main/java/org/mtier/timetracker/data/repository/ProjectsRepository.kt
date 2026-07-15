package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.AddProjectRequest
import org.mtier.timetracker.data.api.dto.EditProjectRequest
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.api.dto.ProjectTableRowDto
import org.mtier.timetracker.data.api.throwOnError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectsRepository
    @Inject
    constructor(
        private val api: TimeTrackerApi,
    ) {
        /** Basic project list, e.g. for the Timer's inline project picker. */
        suspend fun getProjects(): List<ProjectDto> = api.getProjects().projects

        suspend fun getProjectsTable(showArchived: Boolean): List<ProjectTableRowDto> =
            api.getProjectsTable(if (showArchived) 1 else 0).items

        suspend fun addProject(
            name: String,
            clientId: Int?,
            color: String,
        ) {
            api.addProject(name, AddProjectRequest(clientId?.toString() ?: "", color)).throwOnError()
        }

        suspend fun editProject(
            id: Int,
            name: String,
            clientId: Int?,
            color: String,
            locked: Boolean,
            archived: Boolean,
            allowedTagIds: List<Int>,
            allowedUserUids: List<String>,
        ) {
            api
                .editProject(
                    id,
                    EditProjectRequest(
                        name = name,
                        clientId = clientId?.toString() ?: "",
                        color = color,
                        locked = if (locked) "1" else "0",
                        archived = if (archived) "1" else "0",
                        allowedTags = allowedTagIds.joinToString(","),
                        allowedUsers = allowedUserUids.joinToString(","),
                    ),
                ).throwOnError()
        }

        suspend fun deleteProjectWithData(id: Int) {
            api.deleteProjectWithData(id).throwOnError()
        }
    }
