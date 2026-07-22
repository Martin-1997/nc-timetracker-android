package org.mtier.timetracker.ui.login

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mtier.timetracker.data.auth.AuthRepository
import org.mtier.timetracker.data.auth.LoginFlowResult
import org.mtier.timetracker.data.auth.LoginFlowSession
import org.mtier.timetracker.data.auth.SsoEvent
import org.mtier.timetracker.data.auth.SsoLoginManager
import org.mtier.timetracker.data.repository.ThemeRepository
import javax.inject.Inject

sealed interface LoginUiState {
    data object EnteringServerUrl : LoginUiState

    data object Connecting : LoginUiState

    data class WaitingForBrowserLogin(
        val browserLoginUrl: String,
    ) : LoginUiState

    data object Success : LoginUiState

    data class Error(
        val message: String,
    ) : LoginUiState
}

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val themeRepository: ThemeRepository,
        private val ssoManager: SsoLoginManager,
    ) : ViewModel() {
        var serverUrlInput by mutableStateOf("")
            private set

        var uiState by mutableStateOf<LoginUiState>(LoginUiState.EnteringServerUrl)
            private set

        /** Computed off the main thread — PackageManager.getPackageInfo() is
         *  blocking Binder IPC and shouldn't run synchronously during Compose
         *  composition. Null until the check completes. */
        var filesAppAvailable by mutableStateOf<Boolean?>(null)
            private set

        init {
            viewModelScope.launch {
                filesAppAvailable = withContext(Dispatchers.IO) { ssoManager.isFilesAppInstalled() }
            }
            viewModelScope.launch {
                ssoManager.events.collect { event ->
                    when (event) {
                        is SsoEvent.AccountPicked -> {
                            authRepository.completeSsoLogin(event.credentials)
                            themeRepository.refresh()
                            uiState = LoginUiState.Success
                        }
                        is SsoEvent.Error -> uiState = LoginUiState.Error(event.message)
                        SsoEvent.Cancelled -> uiState = LoginUiState.EnteringServerUrl
                    }
                    ssoManager.consumeEvent()
                }
            }
        }

        fun startSsoLogin(activity: Activity) = ssoManager.pickAccount(activity)

        fun onServerUrlChanged(value: String) {
            serverUrlInput = value
        }

        @Suppress("TooGenericExceptionCaught")
        fun startLogin() {
            if (serverUrlInput.isBlank()) {
                uiState = LoginUiState.Error("Please enter a valid server address")
                return
            }
            uiState = LoginUiState.Connecting
            viewModelScope.launch {
                try {
                    val session = authRepository.beginLoginFlow(serverUrlInput)
                    uiState = LoginUiState.WaitingForBrowserLogin(session.browserLoginUrl)
                    awaitLogin(session)
                } catch (e: Exception) {
                    uiState = LoginUiState.Error(e.message ?: "Could not reach that server")
                }
            }
        }

        private suspend fun awaitLogin(session: LoginFlowSession) {
            when (val result = authRepository.awaitCompletion(session)) {
                is LoginFlowResult.Success -> {
                    themeRepository.refresh()
                    uiState = LoginUiState.Success
                }
                is LoginFlowResult.TimedOut -> uiState = LoginUiState.Error("Login timed out. Please try again.")
                is LoginFlowResult.Error -> uiState = LoginUiState.Error(result.message)
            }
        }

        fun retry() {
            uiState = LoginUiState.EnteringServerUrl
        }
    }
