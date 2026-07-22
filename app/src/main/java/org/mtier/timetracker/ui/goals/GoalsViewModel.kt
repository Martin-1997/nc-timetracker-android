package org.mtier.timetracker.ui.goals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.api.dto.GoalDto
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.repository.GoalsRepository
import org.mtier.timetracker.data.repository.ProjectsRepository
import javax.inject.Inject

data class GoalsUiState(
    val goals: List<GoalDto> = emptyList(),
    val projects: List<ProjectDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

val GOAL_INTERVALS = listOf("Weekly", "Monthly")

@HiltViewModel
class GoalsViewModel
    @Inject
    constructor(
        private val goalsRepository: GoalsRepository,
        private val projectsRepository: ProjectsRepository,
    ) : ViewModel() {
        var uiState by mutableStateOf(GoalsUiState())
            private set

        var newGoalProjectId by mutableStateOf<Int?>(null)
            private set

        var newGoalHours by mutableStateOf("")
            private set

        var newGoalInterval by mutableStateOf(GOAL_INTERVALS.first())
            private set

        var pendingDelete by mutableStateOf<GoalDto?>(null)
            private set

        init {
            refresh()
            loadProjects()
        }

        fun refresh() {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModelScope.launch {
                runCatching { goalsRepository.getGoals() }
                    .onSuccess { uiState = uiState.copy(goals = it, isLoading = false) }
                    .onFailure { uiState = uiState.copy(isLoading = false, errorMessage = it.message) }
            }
        }

        private fun loadProjects() {
            viewModelScope.launch {
                runCatching { projectsRepository.getProjects() }
                    .onSuccess { uiState = uiState.copy(projects = it) }
            }
        }

        fun onNewGoalProjectChanged(projectId: Int?) {
            newGoalProjectId = projectId
        }

        fun onNewGoalHoursChanged(value: String) {
            newGoalHours = value
        }

        fun onNewGoalIntervalChanged(interval: String) {
            newGoalInterval = interval
        }

        fun addGoal() {
            val hours = newGoalHours.trim()
            if (hours.isEmpty()) return
            viewModelScope.launch {
                runCatching { goalsRepository.addGoal(newGoalProjectId, hours, newGoalInterval) }
                    .onSuccess {
                        newGoalHours = ""
                        newGoalProjectId = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun requestDelete(goal: GoalDto) {
            pendingDelete = goal
        }

        fun cancelDelete() {
            pendingDelete = null
        }

        fun confirmDelete() {
            val goal = pendingDelete ?: return
            viewModelScope.launch {
                runCatching { goalsRepository.deleteGoal(goal.id) }
                    .onSuccess {
                        pendingDelete = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }
    }
