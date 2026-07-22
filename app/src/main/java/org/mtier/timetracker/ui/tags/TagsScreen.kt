package org.mtier.timetracker.ui.tags

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.mtier.timetracker.R
import org.mtier.timetracker.ui.common.NamedEntityCrudScreen

@Composable
fun TagsScreen(
    modifier: Modifier = Modifier,
    viewModel: TagsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    NamedEntityCrudScreen(
        modifier = modifier,
        items = state.tags,
        getId = { it.id },
        getName = { it.name },
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        newName = viewModel.newTagName,
        onNewNameChanged = viewModel::onNewTagNameChanged,
        newNameLabel = stringResource(R.string.tags_new_name),
        onAdd = viewModel::addTag,
        addLabel = stringResource(R.string.tags_add),
        onEditRequested = viewModel::startEditing,
        onDeleteRequested = viewModel::requestDelete,
        editingName = viewModel.editingTag?.let { viewModel.editingName },
        onEditingNameChanged = viewModel::onEditingNameChanged,
        onSaveEdit = viewModel::saveEditing,
        onCancelEdit = viewModel::cancelEditing,
        editDialogTitle = stringResource(R.string.tags_edit),
        pendingDeleteName = viewModel.pendingDelete?.name,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDelete = viewModel::cancelDelete,
    )
}
