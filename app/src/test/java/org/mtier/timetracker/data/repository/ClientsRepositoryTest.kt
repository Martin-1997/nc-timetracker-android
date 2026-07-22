package org.mtier.timetracker.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.api.dto.ClientsResponse
import org.mtier.timetracker.fakes.FakeClientsCache
import org.mtier.timetracker.fakes.FakeTimeTrackerApi

class ClientsRepositoryTest {
    private val api = FakeTimeTrackerApi()
    private val cache = FakeClientsCache()
    private val repository = ClientsRepository(api, cache)

    @Test
    fun `a successful fetch populates the cache`() =
        runTest {
            val client = ClientDto(id = 1, name = "Acme Corp")
            api.clientsResponse = ClientsResponse(listOf(client))

            val result = repository.getClients()

            assertEquals(listOf(client), result)
            assertEquals(listOf(client), cache.get())
        }

    @Test
    fun `a failed fetch falls back to the cached list instead of throwing`() =
        runTest {
            val cachedClient = ClientDto(id = 1, name = "Acme Corp")
            cache.put(listOf(cachedClient))
            api.clientsError = RuntimeException("offline")

            val result = repository.getClients()

            assertEquals(listOf(cachedClient), result)
        }

    @Test
    fun `a failed fetch with nothing cached rethrows the original error`() =
        runTest {
            val networkError = RuntimeException("offline")
            api.clientsError = networkError

            val result = runCatching { repository.getClients() }

            assertSame(networkError, result.exceptionOrNull())
        }
}
