package org.mtier.timetracker.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.auth.AuthRepository
import org.mtier.timetracker.data.auth.LoginFlowResult
import org.mtier.timetracker.data.auth.LoginFlowSession
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
    ) : ViewModel() {
        var serverUrlInput by mutableStateOf("")
            private set

        var uiState by mutableStateOf<LoginUiState>(LoginUiState.EnteringServerUrl)
            private set

        fun onServerUrlChanged(value: String) {
            serverUrlInput = value
        }

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
                is LoginFlowResult.Success -> uiState = LoginUiState.Success
                is LoginFlowResult.TimedOut -> uiState = LoginUiState.Error("Login timed out. Please try again.")
                is LoginFlowResult.Error -> uiState = LoginUiState.Error(result.message)
            }
        }

        fun retry() {
            uiState = LoginUiState.EnteringServerUrl
        }
    }
