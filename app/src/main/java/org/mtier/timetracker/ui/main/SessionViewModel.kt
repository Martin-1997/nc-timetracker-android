package org.mtier.timetracker.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.auth.AuthRepository
import org.mtier.timetracker.data.repository.UsersRepository
import javax.inject.Inject

@HiltViewModel
class SessionViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val usersRepository: UsersRepository,
    ) : ViewModel() {
        /** Gates the Timelines Admin drawer entry, same as the web
         *  frontend's App.vue (`views.filter(... || this.isAdmin)`). */
        var isAdmin by mutableStateOf(false)
            private set

        init {
            refreshIsAdmin()
        }

        /** Re-checked after a fresh login too (see LoginViewModel) — the
         *  initial check above runs before the app is authenticated and
         *  always resolves to false. */
        fun refreshIsAdmin() {
            viewModelScope.launch {
                isAdmin = runCatching { usersRepository.isCurrentUserAdmin() }.getOrDefault(false)
            }
        }

        /**
         * [onComplete] runs whether sign-out succeeds or throws: SessionViewModel
         * is Activity-scoped (see TimeTrackerNavHost — hiltViewModel() is called
         * outside any nav destination), so its viewModelScope outlives the
         * navigation to the Login screen. The caller must wait for this callback
         * before navigating away, or a fast re-login could race the in-flight
         * cache/SSO-account clears and have its fresh writes clobbered.
         */
        @Suppress("TooGenericExceptionCaught")
        fun signOut(onComplete: () -> Unit) {
            viewModelScope.launch {
                try {
                    authRepository.signOut()
                } catch (e: CancellationException) {
                    throw e
                } catch (
                    @Suppress("SwallowedException") e: Exception,
                ) {
                    // Best-effort — still let the caller navigate to Login
                    // even if sign-out partially failed, rather than
                    // stranding the user on a screen for an account they
                    // just tried to leave.
                }
                onComplete()
            }
        }
    }
