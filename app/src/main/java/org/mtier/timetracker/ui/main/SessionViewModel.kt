package org.mtier.timetracker.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

        fun signOut() {
            viewModelScope.launch { authRepository.signOut() }
        }
    }
