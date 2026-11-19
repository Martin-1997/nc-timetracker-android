package org.mtier.timetracker.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mtier.timetracker.fakes.FakeOcsApi

class ServerCapabilitiesRepositoryTest {
    private val api = FakeOcsApi()
    private val repository = ServerCapabilitiesRepository(api)

    @Test
    fun `refresh publishes the server's theming color`() =
        runTest {
            api.capabilities = FakeOcsApi.withTheming(color = "#123456", colorText = "#abcdef")

            repository.refresh()

            assertEquals(ServerThemeColors("#123456", "#abcdef"), repository.colors.value)
        }

    @Test
    fun `refresh defaults the text color to white when the server omits it`() =
        runTest {
            api.capabilities = FakeOcsApi.withTheming(color = "#123456", colorText = null)

            repository.refresh()

            assertEquals(ServerThemeColors("#123456", "#FFFFFF"), repository.colors.value)
        }

    @Test
    fun `refresh leaves colors null when the server has no theming color`() =
        runTest {
            api.capabilities = FakeOcsApi.withTheming(color = null, colorText = null)

            repository.refresh()

            assertNull(repository.colors.value)
        }

    @Test
    fun `refresh is best-effort and swallows network failures`() =
        runTest {
            api.capabilitiesError = java.io.IOException("offline")

            repository.refresh()

            assertNull(repository.colors.value)
        }

    @Test
    fun `refresh publishes the timetracker version and features`() =
        runTest {
            api.capabilities = FakeOcsApi.withTimeTracker("1.1.0", listOf("goals", "pinned_timers"))

            repository.refresh()

            val caps = repository.timetracker.value
            assertTrue(caps.published)
            assertEquals("1.1.0", caps.version)
            assertEquals(listOf("v1"), caps.apiVersions)
            assertTrue(caps.supports(ServerFeature.PINNED_TIMERS))
            assertTrue(caps.supports(ServerFeature.GOALS))
            assertFalse(caps.supports(ServerFeature.TIMELINES))
        }

    @Test
    fun `a server without the timetracker block keeps legacy features only`() =
        runTest {
            api.capabilities = FakeOcsApi.withTheming(color = "#123456", colorText = null)

            repository.refresh()

            val caps = repository.timetracker.value
            assertFalse(caps.published)
            ServerFeature.entries.forEach { assertEquals(it.legacy, caps.supports(it)) }
        }

    @Test
    fun `a failed refresh keeps the last known capabilities`() =
        runTest {
            api.capabilities = FakeOcsApi.withTimeTracker("1.1.0", listOf("pinned_timers"))
            repository.refresh()
            api.capabilitiesError = java.io.IOException("offline")

            repository.refresh()

            assertTrue(repository.timetracker.value.supports(ServerFeature.PINNED_TIMERS))
        }

    @Test
    fun `clear forgets the previous server`() =
        runTest {
            api.capabilities = FakeOcsApi.withTimeTracker("1.1.0", listOf("pinned_timers"))
            repository.refresh()

            repository.clear()

            assertEquals(TimeTrackerCapabilities.UNKNOWN, repository.timetracker.value)
            assertNull(repository.colors.value)
        }
}
