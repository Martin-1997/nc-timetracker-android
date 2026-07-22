package org.mtier.timetracker.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mtier.timetracker.fakes.FakeOcsApi

class ThemeRepositoryTest {
    private val api = FakeOcsApi()
    private val repository = ThemeRepository(api)

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
}
