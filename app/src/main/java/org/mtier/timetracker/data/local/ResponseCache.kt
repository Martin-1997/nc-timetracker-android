package org.mtier.timetracker.data.local

import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.api.dto.TagDto
import javax.inject.Inject
import javax.inject.Singleton

/** [get] returns null on a cache miss (nothing cached yet) — that's a
 *  different case from "server reachable, list legitimately empty", so
 *  repositories can tell "fall back to cache" apart from "cache also has
 *  nothing, rethrow the original network error" (see PLAN.md §4). */
interface ProjectsCache {
    suspend fun get(): List<ProjectDto>?

    suspend fun put(projects: List<ProjectDto>)
}

interface ClientsCache {
    suspend fun get(): List<ClientDto>?

    suspend fun put(clients: List<ClientDto>)
}

interface TagsCache {
    suspend fun get(): List<TagDto>?

    suspend fun put(tags: List<TagDto>)
}

@Singleton
class RoomProjectsCache
    @Inject
    constructor(
        private val dao: ProjectCacheDao,
    ) : ProjectsCache {
        override suspend fun get(): List<ProjectDto>? = dao.getAll().takeIf { it.isNotEmpty() }?.map { it.toDto() }

        override suspend fun put(projects: List<ProjectDto>) = dao.replaceAll(projects.map { it.toEntity() })
    }

@Singleton
class RoomClientsCache
    @Inject
    constructor(
        private val dao: ClientCacheDao,
    ) : ClientsCache {
        override suspend fun get(): List<ClientDto>? = dao.getAll().takeIf { it.isNotEmpty() }?.map { it.toDto() }

        override suspend fun put(clients: List<ClientDto>) = dao.replaceAll(clients.map { it.toEntity() })
    }

@Singleton
class RoomTagsCache
    @Inject
    constructor(
        private val dao: TagCacheDao,
    ) : TagsCache {
        override suspend fun get(): List<TagDto>? = dao.getAll().takeIf { it.isNotEmpty() }?.map { it.toDto() }

        override suspend fun put(tags: List<TagDto>) = dao.replaceAll(tags.map { it.toEntity() })
    }
