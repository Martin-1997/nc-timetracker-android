package org.mtier.timetracker.ui.timelinesadmin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.api.dto.TimelineDto
import org.mtier.timetracker.data.repository.TimelinesRepository
import org.mtier.timetracker.data.repository.UsersRepository
import javax.inject.Inject

data class TimelinesAdminUiState(
    val timelines: List<TimelineDto> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

val TIMELINE_STATUSES = listOf("pending", "approved", "rejected")

@HiltViewModel
class TimelinesAdminViewModel
    @Inject
    constructor(
        private val timelinesRepository: TimelinesRepository,
        private val usersRepository: UsersRepository,
    ) : ViewModel() {
        var uiState by mutableStateOf(TimelinesAdminUiState())
            private set

        var editing by mutableStateOf<TimelineDto?>(null)
            private set

        var editStatus by mutableStateOf(TIMELINE_STATUSES.first())
            private set

        init {
            viewModelScope.launch {
                val isAdmin = runCatching { usersRepository.isCurrentUserAdmin() }.getOrDefault(false)
                uiState = uiState.copy(isAdmin = isAdmin)
                if (isAdmin) refresh()
            }
        }

        fun refresh() {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModelScope.launch {
                runCatching { timelinesRepository.getTimelinesAdmin() }
                    .onSuccess { uiState = uiState.copy(timelines = it, isLoading = false) }
                    .onFailure { uiState = uiState.copy(isLoading = false, errorMessage = it.message) }
            }
        }

        fun startEditing(timeline: TimelineDto) {
            editing = timeline
            editStatus = timeline.status
        }

        fun onEditStatusChanged(status: String) {
            editStatus = status
        }

        fun cancelEditing() {
            editing = null
        }

        fun saveStatus() {
            val timeline = editing ?: return
            viewModelScope.launch {
                runCatching { timelinesRepository.editStatus(timeline.id, editStatus) }
                    .onSuccess {
                        editing = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }
    }
