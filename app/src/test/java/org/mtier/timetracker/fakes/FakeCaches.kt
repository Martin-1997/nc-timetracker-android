package org.mtier.timetracker.fakes

import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.api.dto.TagDto
import org.mtier.timetracker.data.local.ClientsCache
import org.mtier.timetracker.data.local.ProjectsCache
import org.mtier.timetracker.data.local.TagsCache

/** In-memory stand-ins for the Room-backed caches — avoids pulling Room
 *  (which needs a real Android SQLite driver) into plain JVM unit tests. */
class FakeProjectsCache : ProjectsCache {
    private var stored: List<ProjectDto>? = null

    override suspend fun get(): List<ProjectDto>? = stored

    override suspend fun put(projects: List<ProjectDto>) {
        stored = projects
    }
}

class FakeClientsCache : ClientsCache {
    private var stored: List<ClientDto>? = null

    override suspend fun get(): List<ClientDto>? = stored

    override suspend fun put(clients: List<ClientDto>) {
        stored = clients
    }
}

class FakeTagsCache : TagsCache {
    private var stored: List<TagDto>? = null

    override suspend fun get(): List<TagDto>? = stored

    override suspend fun put(tags: List<TagDto>) {
        stored = tags
    }
}
