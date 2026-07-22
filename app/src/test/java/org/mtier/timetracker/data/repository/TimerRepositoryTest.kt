package org.mtier.timetracker.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mtier.timetracker.data.api.ApiException
import org.mtier.timetracker.data.api.dto.StartTimerRequest
import org.mtier.timetracker.fakes.FakeTimeTrackerApi
import java.time.Instant

class TimerRepositoryTest {
    private val api = FakeTimeTrackerApi()
    private val repository = TimerRepository(api)

    @Test
    fun `getWorkIntervals converts Instants to epoch seconds and forwards tz offset`() =
        runTest {
            val from = Instant.ofEpochSecond(1_000)
            val to = Instant.ofEpochSecond(2_000)

            repository.getWorkIntervals(from, to)

            val (calledFrom, calledTo, calledTzOffset) = api.getWorkIntervalsCalls.single()
            assertEquals(1_000L, calledFrom)
            assertEquals(2_000L, calledTo)
            assertEquals(currentTzOffsetMinutes(), calledTzOffset)
        }

    @Test
    fun `startTimer double-encodes the name and joins tag ids with commas`() =
        runTest {
            repository.startTimer("no description", 5, listOf("1", "2", "3"))

            val (name, body) = api.startTimerCalls.single()
            assertEquals("no%2Bdescription", name)
            assertEquals(StartTimerRequest("5", "1,2,3"), body)
        }

    @Test
    fun `startTimer sends an empty projectId when none is selected`() =
        runTest {
            repository.startTimer("task", null, emptyList())

            val (name, body) = api.startTimerCalls.single()
            assertEquals("task", name)
            assertEquals(StartTimerRequest("", ""), body)
        }

    @Test
    fun `startTimer throws ApiException when the server reports an error`() =
        runTest {
            api.startTimerErrorMessage = "locked project"

            val result = runCatching { repository.startTimer("task", null, emptyList()) }

            assertTrue(result.exceptionOrNull() is ApiException)
            assertEquals("locked project", result.exceptionOrNull()?.message)
        }
}
