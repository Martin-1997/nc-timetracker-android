package org.mtier.timetracker.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.mtier.timetracker.R
import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.api.dto.ProjectTableRowDto
import org.mtier.timetracker.ui.theme.NcBlue

private fun parseColor(hex: String?): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex ?: "#0082C9")) }.getOrDefault(NcBlue)

@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState
    var colorPickerTarget by remember { mutableStateOf<ColorPickerTarget?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        NewProjectForm(
            name = viewModel.newProjectName,
            onNameChanged = viewModel::onNewProjectNameChanged,
            clients = state.clients,
            selectedClientId = viewModel.newProjectClientId,
            onClientChanged = viewModel::onNewProjectClientChanged,
            color = viewModel.newProjectColor,
            onPickColor = { colorPickerTarget = ColorPickerTarget.NewProject },
            onAdd = viewModel::addProject,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Switch(checked = state.showArchived, onCheckedChange = viewModel::toggleShowArchived)
            Text(
                text = stringResource(R.string.projects_show_archived),
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        if (state.isLoading && state.projects.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        } else {
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(state.projects, key = { it.id }) { project ->
                    ProjectRow(project, onClick = { viewModel.startEditing(project) })
                    HorizontalDivider()
                }
            }
        }
    }

    viewModel.editState?.let { editState ->
        EditProjectDialog(
            state = editState,
            clients = state.clients,
            onNameChanged = { name -> viewModel.updateEditState { it.copy(name = name) } },
            onClientChanged = { id -> viewModel.updateEditState { it.copy(clientId = id) } },
            onPickColor = { colorPickerTarget = ColorPickerTarget.EditProject },
            onLockedChanged = { locked -> viewModel.updateEditState { it.copy(locked = locked) } },
            onArchivedChanged = { archived -> viewModel.updateEditState { it.copy(archived = archived) } },
            onSave = viewModel::saveEditing,
            onCancel = viewModel::cancelEditing,
            onDelete = { editState.project?.let(viewModel::requestDelete) },
        )
    }

    colorPickerTarget?.let { target ->
        ColorPickerDialog(
            onColorSelected = { color ->
                when (target) {
                    ColorPickerTarget.NewProject -> viewModel.onNewProjectColorChanged(color)
                    ColorPickerTarget.EditProject -> viewModel.updateEditState { it.copy(color = color) }
                }
                colorPickerTarget = null
            },
            onDismiss = { colorPickerTarget = null },
        )
    }

    viewModel.pendingDelete?.let { project ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text(stringResource(R.string.timer_delete_confirm_title)) },
            text = { Text(project.name) },
            confirmButton = {
                Button(onClick = viewModel::confirmDelete) { Text(stringResource(R.string.common_confirm)) }
            },
            dismissButton = {
                Button(onClick = viewModel::cancelDelete) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

private enum class ColorPickerTarget { NewProject, EditProject }

@Composable
private fun ColorSwatch(
    color: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 32.dp,
) {
    androidx.compose.foundation.layout.Box(
        modifier =
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick),
    )
}

@Composable
private fun NewProjectForm(
    name: String,
    onNameChanged: (String) -> Unit,
    clients: List<ClientDto>,
    selectedClientId: Int?,
    onClientChanged: (Int?) -> Unit,
    color: String,
    onPickColor: () -> Unit,
    onAdd: () -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                label = { Text(stringResource(R.string.projects_new_name)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            ColorSwatch(parseColor(color), onClick = onPickColor, size = 40.dp)
        }
        ClientDropdown(
            clients = clients,
            selectedClientId = selectedClientId,
            onClientChanged = onClientChanged,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(stringResource(R.string.projects_add))
        }
    }
}

/**
 * A plain clickable field + [DropdownMenu] rather than
 * ExposedDropdownMenuBox/menuAnchor — that newer API's shape has changed
 * across Compose Material3 versions, and this simpler pattern has been
 * stable for much longer.
 */
@Composable
private fun ClientDropdown(
    clients: List<ClientDto>,
    selectedClientId: Int?,
    onClientChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = clients.firstOrNull { it.id == selectedClientId }?.name ?: ""

    Column(modifier = modifier) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.projects_client)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("—") },
                onClick = {
                    onClientChanged(null)
                    expanded = false
                },
            )
            clients.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.name) },
                    onClick = {
                        onClientChanged(client.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ProjectRow(
    project: ProjectTableRowDto,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ColorSwatch(parseColor(project.color), onClick = onClick)
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(text = project.name)
            project.client?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
        }
        if (project.locked == 1) {
            Text(text = stringResource(R.string.projects_locked), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EditProjectDialog(
    state: ProjectEditState,
    clients: List<ClientDto>,
    onNameChanged: (String) -> Unit,
    onClientChanged: (Int?) -> Unit,
    onPickColor: () -> Unit,
    onLockedChanged: (Boolean) -> Unit,
    onArchivedChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.projects_edit)) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChanged,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    ColorSwatch(parseColor(state.color), onClick = onPickColor, size = 40.dp)
                }
                ClientDropdown(
                    clients = clients,
                    selectedClientId = state.clientId,
                    onClientChanged = onClientChanged,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Checkbox(checked = state.locked, onCheckedChange = onLockedChanged)
                    Text(stringResource(R.string.projects_locked))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.archived, onCheckedChange = onArchivedChanged)
                    Text(stringResource(R.string.projects_archived))
                }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text(stringResource(R.string.projects_edit)) } },
        dismissButton = {
            Row {
                Button(onClick = onDelete) { Text(stringResource(R.string.projects_delete)) }
                Button(onClick = onCancel, modifier = Modifier.padding(start = 8.dp)) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        },
    )
}

private const val COLOR_PICKER_GRID_COLUMNS = 6

@Composable
private fun ColorPickerDialog(
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.projects_color)) },
        text = {
            LazyVerticalGrid(columns = GridCells.Fixed(COLOR_PICKER_GRID_COLUMNS)) {
                items(PROJECT_COLOR_PALETTE) { hex ->
                    ColorSwatch(
                        color = parseColor(hex),
                        onClick = { onColorSelected(hex) },
                        size = 36.dp,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}
