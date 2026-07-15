package org.mtier.timetracker.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.mtier.timetracker.data.auth.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    fun signOut() = authRepository.signOut()
}
