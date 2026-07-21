package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.AddProjectRequest
import org.mtier.timetracker.data.api.dto.EditProjectRequest
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.api.dto.ProjectTableRowDto
import org.mtier.timetracker.data.api.throwOnError
import org.mtier.timetracker.data.local.ProjectsCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectsRepository
    @Inject
    constructor(
        private val api: TimeTrackerApi,
        private val cache: ProjectsCache,
    ) {
        /**
         * Basic project list, e.g. for the Timer's inline project picker.
         * Network-first: a successful response refreshes the cache; a
         * failed one (offline, server down) falls back to the last cached
         * list rather than leaving the picker empty, and only rethrows if
         * there's nothing cached either (PLAN.md §4's "thin response
         * cache" — this list is what makes it worth having: mostly-static
         * reference data, not the timer's own live state).
         */
        suspend fun getProjects(): List<ProjectDto> =
            runCatching { api.getProjects().projects }
                .onSuccess { cache.put(it) }
                .getOrElse { networkError -> cache.get() ?: throw networkError }

        suspend fun getProjectsTable(showArchived: Boolean): List<ProjectTableRowDto> =
            api.getProjectsTable(if (showArchived) 1 else 0).items

        suspend fun addProject(
            name: String,
            clientId: Int?,
            color: String,
        ) {
            api.addProject(name, AddProjectRequest(clientId?.toString() ?: "", color)).throwOnError()
        }

        @Suppress("LongParameterList")
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
