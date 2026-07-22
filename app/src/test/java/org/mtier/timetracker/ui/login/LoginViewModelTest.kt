@file:OptIn(ExperimentalCoroutinesApi::class)

package org.mtier.timetracker.ui.login

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mtier.timetracker.MainDispatcherRule
import org.mtier.timetracker.data.api.dto.OcsCapabilitiesDto
import org.mtier.timetracker.data.api.dto.OcsCapabilitiesSectionsDto
import org.mtier.timetracker.data.api.dto.OcsThemingDto
import org.mtier.timetracker.data.auth.AuthRepository
import org.mtier.timetracker.data.auth.Credentials
import org.mtier.timetracker.data.auth.SsoEvent
import org.mtier.timetracker.data.repository.ThemeRepository
import org.mtier.timetracker.fakes.FakeCacheStore
import org.mtier.timetracker.fakes.FakeCredentialStore
import org.mtier.timetracker.fakes.FakeOcsApi
import org.mtier.timetracker.fakes.FakeSsoAccountManager
import org.mtier.timetracker.fakes.FakeSsoLoginManager

/**
 * AuthRepository.beginLoginFlow() builds its own Retrofit client for
 * whatever server URL the user typed, so it can't be swapped out here the
 * way the other repositories' TimeTrackerApi/OcsApi dependencies can — see
 * AuthRepositoryTest (which drives AuthRepository.awaitCompletion directly
 * against a FakeLoginFlowV2Api) for coverage of the actual Login Flow v2
 * polling behavior. This class covers the pure, network-free branches of
 * LoginViewModel's state machine, plus the Files-app SSO event handling
 * (issue #2) via FakeSsoLoginManager.emit().
 */
class LoginViewModelTest {
    // LoginViewModel's init{} collects NextcloudSsoManager.events, which
    // needs Dispatchers.Main initialized even though these tests never
    // trigger an SSO event.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val credentialStore = FakeCredentialStore()
    private val themeApi = FakeOcsApi()
    private val themeRepository = ThemeRepository(themeApi)
    private val ssoAccountManager = FakeSsoAccountManager()
    private val ssoLoginManager = FakeSsoLoginManager()

    private fun viewModel(): LoginViewModel =
        LoginViewModel(
            AuthRepository(credentialStore, ssoAccountManager, FakeCacheStore()),
            themeRepository,
            ssoLoginManager,
        )

    @Test
    fun `blank server url is rejected without contacting the repository`() {
        val viewModel = viewModel()
        viewModel.onServerUrlChanged("   ")

        viewModel.startLogin()

        assertTrue(viewModel.uiState is LoginUiState.Error)
    }

    @Test
    fun `retry resets back to entering the server url`() {
        val viewModel = viewModel()
        viewModel.onServerUrlChanged("")
        viewModel.startLogin()

        viewModel.retry()

        assertEquals(LoginUiState.EnteringServerUrl, viewModel.uiState)
    }

    @Test
    fun `filesAppAvailable reflects the SSO manager's installed check once it completes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            ssoLoginManager.filesAppInstalled = true
            val viewModel = viewModel()

            advanceUntilIdle()
            // The check hops through the real Dispatchers.IO (not the
            // virtual test dispatcher), so give it a moment to actually
            // complete before draining the continuation it posts back.
            withContext(Dispatchers.Default) { delay(50) }
            advanceUntilIdle()

            assertEquals(true, viewModel.filesAppAvailable)
        }

    @Test
    fun `AccountPicked saves credentials, refreshes the theme, moves to Success, and consumes the event`() =
        runTest(mainDispatcherRule.testDispatcher) {
            themeApi.capabilities =
                OcsCapabilitiesDto(OcsCapabilitiesSectionsDto(OcsThemingDto(color = "#112233", colorText = "#ffffff")))
            val credentials = Credentials("https://cloud.example.com", "alice", "sso-token-123", isSso = true)
            val viewModel = viewModel()

            ssoLoginManager.emit(SsoEvent.AccountPicked(credentials))
            advanceUntilIdle()

            assertEquals(credentials, credentialStore.load())
            assertEquals("#112233", themeRepository.colors.value?.primaryHex)
            assertEquals(LoginUiState.Success, viewModel.uiState)
            assertEquals(1, ssoLoginManager.consumeEventCallCount)
        }

    @Test
    fun `Error moves to an Error state with the event's message`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()

            ssoLoginManager.emit(SsoEvent.Error("Nextcloud app not installed"))
            advanceUntilIdle()

            assertEquals(LoginUiState.Error("Nextcloud app not installed"), viewModel.uiState)
            assertEquals(1, ssoLoginManager.consumeEventCallCount)
        }

    @Test
    fun `Cancelled resets back to entering the server url`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            viewModel.onServerUrlChanged("   ")
            viewModel.startLogin()
            assertTrue(viewModel.uiState is LoginUiState.Error)

            ssoLoginManager.emit(SsoEvent.Cancelled)
            advanceUntilIdle()

            assertEquals(LoginUiState.EnteringServerUrl, viewModel.uiState)
            assertEquals(1, ssoLoginManager.consumeEventCallCount)
        }
}
