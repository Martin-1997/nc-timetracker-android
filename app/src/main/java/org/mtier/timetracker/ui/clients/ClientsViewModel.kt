package org.mtier.timetracker.ui.clients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.repository.ClientsRepository
import javax.inject.Inject

data class ClientsUiState(
    val clients: List<ClientDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ClientsViewModel
    @Inject
    constructor(
        private val repository: ClientsRepository,
    ) : ViewModel() {
        var uiState by mutableStateOf(ClientsUiState())
            private set

        var newClientName by mutableStateOf("")
            private set

        var editingClient by mutableStateOf<ClientDto?>(null)
            private set

        var editingName by mutableStateOf("")
            private set

        var pendingDelete by mutableStateOf<ClientDto?>(null)
            private set

        init {
            refresh()
        }

        fun refresh() {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModelScope.launch {
                runCatching { repository.getClients() }
                    .onSuccess { uiState = uiState.copy(clients = it, isLoading = false) }
                    .onFailure { uiState = uiState.copy(isLoading = false, errorMessage = it.message) }
            }
        }

        fun onNewClientNameChanged(value: String) {
            newClientName = value
        }

        fun addClient() {
            val name = newClientName.trim()
            if (name.isEmpty()) return
            viewModelScope.launch {
                runCatching { repository.addClient(name) }
                    .onSuccess {
                        newClientName = ""
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun startEditing(client: ClientDto) {
            editingClient = client
            editingName = client.name
        }

        fun onEditingNameChanged(value: String) {
            editingName = value
        }

        fun cancelEditing() {
            editingClient = null
        }

        fun saveEditing() {
            val client = editingClient ?: return
            viewModelScope.launch {
                runCatching { repository.editClient(client.id, editingName.trim()) }
                    .onSuccess {
                        editingClient = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun requestDelete(client: ClientDto) {
            pendingDelete = client
        }

        fun cancelDelete() {
            pendingDelete = null
        }

        fun confirmDelete() {
            val client = pendingDelete ?: return
            viewModelScope.launch {
                runCatching { repository.deleteClient(client.id) }
                    .onSuccess {
                        pendingDelete = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }
    }
