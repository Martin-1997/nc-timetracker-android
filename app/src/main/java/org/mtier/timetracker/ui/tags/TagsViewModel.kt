package org.mtier.timetracker.ui.tags

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.api.dto.TagDto
import org.mtier.timetracker.data.repository.TagsRepository
import javax.inject.Inject

data class TagsUiState(
    val tags: List<TagDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class TagsViewModel
    @Inject
    constructor(
        private val repository: TagsRepository,
    ) : ViewModel() {
        var uiState by mutableStateOf(TagsUiState())
            private set

        var newTagName by mutableStateOf("")
            private set

        var editingTag by mutableStateOf<TagDto?>(null)
            private set

        var editingName by mutableStateOf("")
            private set

        var pendingDelete by mutableStateOf<TagDto?>(null)
            private set

        init {
            refresh()
        }

        fun refresh() {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModelScope.launch {
                runCatching { repository.getTags() }
                    .onSuccess { uiState = uiState.copy(tags = it, isLoading = false) }
                    .onFailure { uiState = uiState.copy(isLoading = false, errorMessage = it.message) }
            }
        }

        fun onNewTagNameChanged(value: String) {
            newTagName = value
        }

        fun addTag() {
            val name = newTagName.trim()
            if (name.isEmpty()) return
            viewModelScope.launch {
                runCatching { repository.addTag(name) }
                    .onSuccess {
                        newTagName = ""
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun startEditing(tag: TagDto) {
            editingTag = tag
            editingName = tag.name
        }

        fun onEditingNameChanged(value: String) {
            editingName = value
        }

        fun cancelEditing() {
            editingTag = null
        }

        fun saveEditing() {
            val tag = editingTag ?: return
            viewModelScope.launch {
                runCatching { repository.editTag(tag.id, editingName.trim()) }
                    .onSuccess {
                        editingTag = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun requestDelete(tag: TagDto) {
            pendingDelete = tag
        }

        fun cancelDelete() {
            pendingDelete = null
        }

        fun confirmDelete() {
            val tag = pendingDelete ?: return
            viewModelScope.launch {
                runCatching { repository.deleteTag(tag.id) }
                    .onSuccess {
                        pendingDelete = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }
    }
