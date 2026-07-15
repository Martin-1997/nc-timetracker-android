package org.mtier.timetracker.ui.timer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.mtier.timetracker.R
import org.mtier.timetracker.data.api.dto.TagDto
import org.mtier.timetracker.data.api.dto.WorkIntervalItemDto
import org.mtier.timetracker.ui.common.DATE_RANGE_PRESETS
import org.mtier.timetracker.ui.common.DatePickerButton
import org.mtier.timetracker.ui.common.DateTimePickerButton
import org.mtier.timetracker.ui.common.resolvePresetRange

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        TopBar(viewModel)

        DateRangeRow(
            from = viewModel.rangeFrom,
            to = viewModel.rangeTo,
            onFromChanged = viewModel::onRangeFromChanged,
            onToChanged = viewModel::onRangeToChanged,
            onPreset = viewModel::applyPreset,
            onRefresh = viewModel::refresh,
        )

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        if (state.isLoading && state.days.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        } else {
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                state.days.forEach { dayGroup ->
                    item {
                        Text(
                            text = dayGroup.label,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                    items(dayGroup.items, key = { it.id }) { workItem ->
                        WorkIntervalRow(workItem, viewModel)
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    viewModel.editingNameDetails?.let { editState ->
        AlertDialog(
            onDismissRequest = viewModel::cancelEditingNameDetails,
            title = { Text(stringResource(R.string.timer_edit_entry)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editState.name,
                        onValueChange = { name -> viewModel.updateEditingNameDetails { it.copy(name = name) } },
                        label = { Text(stringResource(R.string.timer_name)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = editState.details,
                        onValueChange = { details -> viewModel.updateEditingNameDetails { it.copy(details = details) } },
                        label = { Text(stringResource(R.string.timer_details)) },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = { Button(onClick = viewModel::saveNameDetails) { Text(stringResource(R.string.timer_save)) } },
            dismissButton = {
                Button(onClick = viewModel::cancelEditingNameDetails) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }

    viewModel.editingTime?.let { editState ->
        AlertDialog(
            onDismissRequest = viewModel::cancelEditingTime,
            title = { Text(stringResource(R.string.timer_edit_time)) },
            text = {
                Column {
                    Text(stringResource(R.string.timer_start_time))
                    DateTimePickerButton(
                        value = editState.start,
                        onValueChanged = { start -> viewModel.updateEditingTime { it.copy(start = start) } },
                    )
                    Text(stringResource(R.string.timer_end_time), modifier = Modifier.padding(top = 8.dp))
                    DateTimePickerButton(
                        value = editState.end,
                        onValueChanged = { end -> viewModel.updateEditingTime { it.copy(end = end) } },
                    )
                }
            },
            confirmButton = { Button(onClick = viewModel::saveEditingTime) { Text(stringResource(R.string.timer_save)) } },
            dismissButton = {
                Button(onClick = viewModel::cancelEditingTime) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }

    viewModel.manualEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = viewModel::cancelManualEntry,
            title = { Text(stringResource(R.string.timer_manual_entry)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = entry.name,
                        onValueChange = { name -> viewModel.updateManualEntry { it.copy(name = name) } },
                        label = { Text(stringResource(R.string.timer_name)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = entry.details,
                        onValueChange = { details -> viewModel.updateManualEntry { it.copy(details = details) } },
                        label = { Text(stringResource(R.string.timer_details)) },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(stringResource(R.string.timer_start_time), modifier = Modifier.padding(top = 8.dp))
                    DateTimePickerButton(
                        value = entry.start,
                        onValueChanged = { start -> viewModel.updateManualEntry { it.copy(start = start) } },
                    )
                    Text(stringResource(R.string.timer_end_time), modifier = Modifier.padding(top = 8.dp))
                    DateTimePickerButton(
                        value = entry.end,
                        onValueChanged = { end -> viewModel.updateManualEntry { it.copy(end = end) } },
                    )
                }
            },
            confirmButton = { Button(onClick = viewModel::saveManualEntry) { Text(stringResource(R.string.timer_add_entry)) } },
            dismissButton = {
                Button(onClick = viewModel::cancelManualEntry) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }

    viewModel.pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text(stringResource(R.string.timer_delete_confirm_title)) },
            text = { Text(stringResource(R.string.timer_delete_confirm_message)) },
            confirmButton = { Button(onClick = viewModel::confirmDelete) { Text(stringResource(R.string.common_confirm)) } },
            dismissButton = { Button(onClick = viewModel::cancelDelete) { Text(stringResource(R.string.common_cancel)) } },
        )
    }
}

@Composable
private fun TopBar(viewModel: TimerViewModel) {
    val state = viewModel.uiState
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = viewModel.workInput,
            onValueChange = viewModel::onWorkInputChanged,
            label = { Text(stringResource(R.string.timer_what_have_you_done)) },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Button(onClick = viewModel::onSubmitWorkInput, modifier = Modifier.padding(start = 8.dp)) {
            Text(stringResource(if (state.isRunning) R.string.timer_stop else R.string.timer_start))
        }
    }
    if (state.isRunning) {
        Text(
            text = formatElapsed(state.liveElapsedSeconds),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    OutlinedButton(onClick = viewModel::openManualEntry, modifier = Modifier.padding(top = 8.dp)) {
        Text(stringResource(R.string.timer_manual_entry))
    }
}

@Composable
private fun DateRangeRow(
    from: java.time.Instant,
    to: java.time.Instant,
    onFromChanged: (java.time.Instant) -> Unit,
    onToChanged: (java.time.Instant) -> Unit,
    onPreset: (Pair<java.time.Instant, java.time.Instant>) -> Unit,
    onRefresh: () -> Unit,
) {
    var presetMenuExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
        Column {
            OutlinedButton(onClick = { presetMenuExpanded = true }) {
                Text(stringResource(R.string.timer_quick_range))
            }
            DropdownMenu(expanded = presetMenuExpanded, onDismissRequest = { presetMenuExpanded = false }) {
                DATE_RANGE_PRESETS.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.label) },
                        onClick = {
                            presetMenuExpanded = false
                            resolvePresetRange(preset.key)?.let(onPreset)
                        },
                    )
                }
            }
        }
        DatePickerButton(value = from, onValueChanged = onFromChanged)
        Text("–", modifier = Modifier.padding(horizontal = 4.dp))
        DatePickerButton(value = to, onValueChanged = onToChanged)
        Button(onClick = onRefresh, modifier = Modifier.padding(start = 4.dp)) {
            Text(stringResource(R.string.common_retry))
        }
    }
}

@Composable
private fun WorkIntervalRow(
    item: WorkIntervalItemDto,
    viewModel: TimerViewModel,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.IconButton(onClick = { viewModel.resume(item) }) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Filled.PlayArrow,
                    contentDescription = null,
                )
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable { viewModel.openEditNameDetails(item) },
            ) {
                Text(text = item.name)
                item.details?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
            androidx.compose.material3.IconButton(onClick = { viewModel.requestDelete(item) }) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Filled.Delete,
                    contentDescription = null,
                )
            }
        }

        ProjectSelector(item, viewModel)
        TagsSelector(item, viewModel)

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            OutlinedButton(onClick = { viewModel.openEditTime(item) }) {
                Text(formatDuration(item.duration))
            }
            val status = viewModel.costStatus[item.id]
            OutlinedTextField(
                value = viewModel.costDraft(item),
                onValueChange = { viewModel.onCostDraftChanged(item, it) },
                label = { Text(stringResource(R.string.timer_cost)) },
                singleLine = true,
                isError = status == CostStatus.ERROR,
                modifier =
                    Modifier
                        .padding(start = 8.dp)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) viewModel.commitCost(item)
                        },
            )
        }
    }
}

@Composable
private fun ProjectSelector(
    item: WorkIntervalItemDto,
    viewModel: TimerViewModel,
) {
    var expanded by remember { mutableStateOf(false) }
    val projects = viewModel.uiState.projects
    val selectedName =
        projects.firstOrNull { it.id == item.projectId }?.name
            ?: stringResource(R.string.timer_project)

    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.padding(top = 4.dp)) {
            Text(selectedName)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("—") }, onClick = {
                viewModel.updateProject(item, null)
                expanded = false
            })
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        viewModel.updateProject(item, project.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TagsSelector(
    item: WorkIntervalItemDto,
    viewModel: TimerViewModel,
) {
    var showDialog by remember { mutableStateOf(false) }
    val label =
        if (item.tags.isEmpty()) {
            stringResource(R.string.timer_tags)
        } else {
            item.tags.joinToString(", ") { it.name }
        }

    OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.padding(top = 4.dp)) {
        Text(label)
    }

    if (showDialog) {
        TagsPickerDialog(
            allTags = viewModel.uiState.tags,
            selectedTagIds = item.tags.map { it.id }.toSet(),
            onDismiss = { showDialog = false },
            onConfirm = { selectedIdsOrNames ->
                viewModel.updateTags(item, selectedIdsOrNames)
                showDialog = false
            },
        )
    }
}

@Composable
private fun TagsPickerDialog(
    allTags: List<TagDto>,
    selectedTagIds: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    var selected by remember { mutableStateOf(selectedTagIds) }
    var newTagName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.timer_tags)) },
        text = {
            Column {
                allTags.forEach { tag ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Checkbox(
                            checked = selected.contains(tag.id),
                            onCheckedChange = { checked ->
                                selected = if (checked) selected + tag.id else selected - tag.id
                            },
                        )
                        Text(tag.name)
                    }
                }
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text(stringResource(R.string.tags_new_name)) },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val ids = selected.map { it.toString() }
                val extra = newTagName.trim().takeIf { it.isNotEmpty() }?.let { listOf(it) } ?: emptyList()
                onConfirm(ids + extra)
            }) { Text(stringResource(R.string.timer_save)) }
        },
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}

private fun formatDuration(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

private fun formatElapsed(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
