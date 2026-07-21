package org.mtier.timetracker.ui.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mtier.timetracker.MainDispatcherRule
import org.mtier.timetracker.data.auth.AuthRepository
import org.mtier.timetracker.data.repository.ThemeRepository
import org.mtier.timetracker.fakes.FakeClientsCache
import org.mtier.timetracker.fakes.FakeCredentialStore
import org.mtier.timetracker.fakes.FakeOcsApi
import org.mtier.timetracker.fakes.FakeProjectsCache
import org.mtier.timetracker.fakes.FakeSsoAccountManager
import org.mtier.timetracker.fakes.FakeSsoLoginManager
import org.mtier.timetracker.fakes.FakeTagsCache

/**
 * AuthRepository.beginLoginFlow() builds its own Retrofit client for
 * whatever server URL the user typed, so it can't be swapped out here the
 * way the other repositories' TimeTrackerApi/OcsApi dependencies can — see
 * AuthRepositoryTest (which drives AuthRepository.awaitCompletion directly
 * against a FakeLoginFlowV2Api) for coverage of the actual Login Flow v2
 * polling behavior. This class only covers the pure, network-free branches
 * of LoginViewModel's state machine.
 */
class LoginViewModelTest {
    // LoginViewModel's init{} collects NextcloudSsoManager.events, which
    // needs Dispatchers.Main initialized even though these tests never
    // trigger an SSO event.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val credentialStore = FakeCredentialStore()
    private val themeApi = FakeOcsApi()

    private fun viewModel(): LoginViewModel =
        LoginViewModel(
            AuthRepository(
                credentialStore,
                FakeSsoAccountManager(),
                FakeProjectsCache(),
                FakeClientsCache(),
                FakeTagsCache(),
            ),
            ThemeRepository(themeApi),
            FakeSsoLoginManager(),
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
}
