package org.mtier.timetracker.ui.timelinesadmin

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.mtier.timetracker.R
import org.mtier.timetracker.data.api.dto.TimelineDto
import org.mtier.timetracker.ui.common.formatDurationHms
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val CREATED_AT_FORMATTER =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withZone(ZoneId.systemDefault())

@Composable
private fun statusLabel(status: String): String =
    when (status) {
        "approved" -> stringResource(R.string.timelines_admin_approved)
        "rejected" -> stringResource(R.string.timelines_admin_rejected)
        else -> stringResource(R.string.timelines_admin_pending)
    }

@Composable
fun TimelinesAdminScreen(
    modifier: Modifier = Modifier,
    viewModel: TimelinesAdminViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        if (state.isLoading && state.timelines.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        } else {
            LazyColumn {
                items(state.timelines, key = { it.id }) { timeline ->
                    TimelineAdminRow(timeline, onEdit = { viewModel.startEditing(timeline) })
                    HorizontalDivider()
                }
            }
        }
    }

    viewModel.editing?.let {
        AlertDialog(
            onDismissRequest = viewModel::cancelEditing,
            title = { Text(stringResource(R.string.timelines_admin_edit_status)) },
            text = {
                StatusDropdown(
                    selected = viewModel.editStatus,
                    onSelected = viewModel::onEditStatusChanged,
                )
            },
            confirmButton = {
                Button(onClick = viewModel::saveStatus) { Text(stringResource(R.string.timelines_admin_save)) }
            },
            dismissButton = {
                Button(onClick = viewModel::cancelEditing) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

@Composable
private fun TimelineAdminRow(
    timeline: TimelineDto,
    onEdit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = "#${timeline.id} · ${timeline.userUid ?: ""} · ${statusLabel(timeline.status)}")
        Text(text = timeline.timeInterval ?: "", style = MaterialTheme.typography.bodySmall)
        Text(
            text =
                formatDurationHms(timeline.totalDuration.toLongOrNull() ?: 0) +
                    " · " + CREATED_AT_FORMATTER.format(Instant.ofEpochSecond(timeline.createdAt)),
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = onEdit, modifier = Modifier.padding(top = 4.dp)) {
            Text(stringResource(R.string.timelines_admin_edit_status))
        }
    }
}

@Composable
private fun StatusDropdown(
    selected: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(statusLabel(selected))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TIMELINE_STATUSES.forEach { status ->
                DropdownMenuItem(
                    text = { Text(statusLabel(status)) },
                    onClick = {
                        onSelected(status)
                        expanded = false
                    },
                )
            }
        }
    }
}
