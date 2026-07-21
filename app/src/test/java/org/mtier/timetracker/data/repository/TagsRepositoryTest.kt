package org.mtier.timetracker.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.mtier.timetracker.data.api.dto.TagDto
import org.mtier.timetracker.data.api.dto.TagsResponse
import org.mtier.timetracker.fakes.FakeTagsCache
import org.mtier.timetracker.fakes.FakeTimeTrackerApi

class TagsRepositoryTest {
    private val api = FakeTimeTrackerApi()
    private val cache = FakeTagsCache()
    private val repository = TagsRepository(api, cache)

    @Test
    fun `a successful fetch populates the cache`() =
        runTest {
            val tag = TagDto(id = 1, name = "billable")
            api.tagsResponse = TagsResponse(listOf(tag))

            val result = repository.getTags()

            assertEquals(listOf(tag), result)
            assertEquals(listOf(tag), cache.get())
        }

    @Test
    fun `a failed fetch falls back to the cached list instead of throwing`() =
        runTest {
            val cachedTag = TagDto(id = 1, name = "billable")
            cache.put(listOf(cachedTag))
            api.tagsError = RuntimeException("offline")

            val result = repository.getTags()

            assertEquals(listOf(cachedTag), result)
        }

    @Test
    fun `a failed fetch with nothing cached rethrows the original error`() =
        runTest {
            val networkError = RuntimeException("offline")
            api.tagsError = networkError

            val result = runCatching { repository.getTags() }

            assertSame(networkError, result.exceptionOrNull())
        }
}
