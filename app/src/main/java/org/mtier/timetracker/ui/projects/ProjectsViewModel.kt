package org.mtier.timetracker.ui.projects

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.api.dto.ProjectTableRowDto
import org.mtier.timetracker.data.repository.ClientsRepository
import org.mtier.timetracker.data.repository.ProjectsRepository
import javax.inject.Inject

data class ProjectsUiState(
    val projects: List<ProjectTableRowDto> = emptyList(),
    val clients: List<ClientDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showArchived: Boolean = false,
)

data class ProjectEditState(
    val project: ProjectTableRowDto?,
    val name: String,
    val clientId: Int?,
    val color: String,
    val locked: Boolean,
    val archived: Boolean,
)

@HiltViewModel
class ProjectsViewModel
    @Inject
    constructor(
        private val projectsRepository: ProjectsRepository,
        private val clientsRepository: ClientsRepository,
    ) : ViewModel() {
        var uiState by mutableStateOf(ProjectsUiState())
            private set

        var newProjectName by mutableStateOf("")
            private set

        var newProjectClientId by mutableStateOf<Int?>(null)
            private set

        var newProjectColor by mutableStateOf(PROJECT_COLOR_PALETTE.first())
            private set

        var editState by mutableStateOf<ProjectEditState?>(null)
            private set

        var pendingDelete by mutableStateOf<ProjectTableRowDto?>(null)
            private set

        init {
            refresh()
            loadClients()
        }

        fun refresh() {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModelScope.launch {
                runCatching { projectsRepository.getProjectsTable(uiState.showArchived) }
                    .onSuccess { uiState = uiState.copy(projects = it, isLoading = false) }
                    .onFailure { uiState = uiState.copy(isLoading = false, errorMessage = it.message) }
            }
        }

        private fun loadClients() {
            viewModelScope.launch {
                runCatching { clientsRepository.getClients() }
                    .onSuccess { uiState = uiState.copy(clients = it) }
            }
        }

        fun toggleShowArchived(show: Boolean) {
            uiState = uiState.copy(showArchived = show)
            refresh()
        }

        fun onNewProjectNameChanged(value: String) {
            newProjectName = value
        }

        fun onNewProjectClientChanged(clientId: Int?) {
            newProjectClientId = clientId
        }

        fun onNewProjectColorChanged(color: String) {
            newProjectColor = color
        }

        fun addProject() {
            val name = newProjectName.trim()
            if (name.isEmpty()) return
            viewModelScope.launch {
                runCatching { projectsRepository.addProject(name, newProjectClientId, newProjectColor) }
                    .onSuccess {
                        newProjectName = ""
                        newProjectClientId = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun startEditing(project: ProjectTableRowDto) {
            editState =
                ProjectEditState(
                    project = project,
                    name = project.name,
                    clientId = project.clientId,
                    color = project.color ?: PROJECT_COLOR_PALETTE.first(),
                    locked = project.locked == 1,
                    archived = project.archived == 1,
                )
        }

        fun updateEditState(transform: (ProjectEditState) -> ProjectEditState) {
            editState = editState?.let(transform)
        }

        fun cancelEditing() {
            editState = null
        }

        fun saveEditing() {
            val state = editState ?: return
            val project = state.project ?: return
            viewModelScope.launch {
                runCatching {
                    projectsRepository.editProject(
                        id = project.id,
                        name = state.name,
                        clientId = state.clientId,
                        color = state.color,
                        locked = state.locked,
                        archived = state.archived,
                        allowedTagIds = emptyList(),
                        allowedUserUids = emptyList(),
                    )
                }.onSuccess {
                    editState = null
                    refresh()
                }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun requestDelete(project: ProjectTableRowDto) {
            pendingDelete = project
        }

        fun cancelDelete() {
            pendingDelete = null
        }

        fun confirmDelete() {
            val project = pendingDelete ?: return
            viewModelScope.launch {
                runCatching { projectsRepository.deleteProjectWithData(project.id) }
                    .onSuccess {
                        pendingDelete = null
                        editState = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }
    }
