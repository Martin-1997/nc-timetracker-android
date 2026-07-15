package org.mtier.timetracker.ui.clients

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.mtier.timetracker.R
import org.mtier.timetracker.ui.common.NamedEntityCrudScreen

@Composable
fun ClientsScreen(
    modifier: Modifier = Modifier,
    viewModel: ClientsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    NamedEntityCrudScreen(
        modifier = modifier,
        items = state.clients,
        getId = { it.id },
        getName = { it.name },
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        newName = viewModel.newClientName,
        onNewNameChanged = viewModel::onNewClientNameChanged,
        newNameLabel = stringResource(R.string.clients_new_name),
        onAdd = viewModel::addClient,
        addLabel = stringResource(R.string.clients_add),
        onEditRequested = viewModel::startEditing,
        onDeleteRequested = viewModel::requestDelete,
        editingName = viewModel.editingClient?.let { viewModel.editingName },
        onEditingNameChanged = viewModel::onEditingNameChanged,
        onSaveEdit = viewModel::saveEditing,
        onCancelEdit = viewModel::cancelEditing,
        editDialogTitle = stringResource(R.string.clients_edit),
        pendingDeleteName = viewModel.pendingDelete?.name,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDelete = viewModel::cancelDelete,
    )
}
