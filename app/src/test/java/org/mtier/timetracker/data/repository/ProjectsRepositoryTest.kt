package org.mtier.timetracker.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.api.dto.ProjectsResponse
import org.mtier.timetracker.fakes.FakeProjectsCache
import org.mtier.timetracker.fakes.FakeTimeTrackerApi

class ProjectsRepositoryTest {
    private val api = FakeTimeTrackerApi()
    private val cache = FakeProjectsCache()
    private val repository = ProjectsRepository(api, cache)

    @Test
    fun `a successful fetch populates the cache`() =
        runTest {
            val project = ProjectDto(id = 1, name = "Website")
            api.projectsResponse = ProjectsResponse(listOf(project))

            val result = repository.getProjects()

            assertEquals(listOf(project), result)
            assertEquals(listOf(project), cache.get())
        }

    @Test
    fun `a failed fetch falls back to the cached list instead of throwing`() =
        runTest {
            val cachedProject = ProjectDto(id = 1, name = "Website")
            cache.put(listOf(cachedProject))
            api.projectsError = RuntimeException("offline")

            val result = repository.getProjects()

            assertEquals(listOf(cachedProject), result)
        }

    @Test
    fun `a failed fetch with nothing cached rethrows the original error`() =
        runTest {
            val networkError = RuntimeException("offline")
            api.projectsError = networkError

            val result = runCatching { repository.getProjects() }

            assertSame(networkError, result.exceptionOrNull())
        }
}
