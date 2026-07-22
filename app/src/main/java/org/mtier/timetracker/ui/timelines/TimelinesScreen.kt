package org.mtier.timetracker.ui.timelines

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.mtier.timetracker.R
import org.mtier.timetracker.data.api.dto.ReportItemDto
import org.mtier.timetracker.data.api.dto.TimelineDto
import org.mtier.timetracker.ui.common.DateRangeRow
import org.mtier.timetracker.ui.common.KeyDropdown
import org.mtier.timetracker.ui.common.ToggleChipGroup
import org.mtier.timetracker.ui.common.formatDurationHms
import org.mtier.timetracker.ui.common.shareFile
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val TIMELINE_GROUP1_OPTIONS = listOf("project", "client")
private val TIMELINE_GROUP2_OPTIONS = listOf("", "name")
private val TIMELINE_TIMEGROUP_OPTIONS = listOf("day", "week", "month", "year")

private val CREATED_AT_FORMATTER =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withZone(ZoneId.systemDefault())

@Composable
fun TimelinesScreen(
    modifier: Modifier = Modifier,
    viewModel: TimelinesViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row {
            KeyDropdown(
                selected = viewModel.group1,
                options = TIMELINE_GROUP1_OPTIONS,
                labelRes = R.string.reports_group_by,
                onSelected = viewModel::onGroup1Changed,
                modifier = Modifier.weight(1f),
            )
            KeyDropdown(
                selected = viewModel.group2,
                options = TIMELINE_GROUP2_OPTIONS,
                labelRes = R.string.reports_then_by,
                onSelected = viewModel::onGroup2Changed,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
        }
        KeyDropdown(
            selected = viewModel.timegroup,
            options = TIMELINE_TIMEGROUP_OPTIONS,
            labelRes = R.string.reports_interval,
            onSelected = viewModel::onTimegroupChanged,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        Text(
            text = stringResource(R.string.reports_filter_projects),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        ToggleChipGroup(
            items = state.projects.map { it.id to it.name },
            selectedIds = viewModel.filterProjectIds,
            onToggle = viewModel::toggleFilterProject,
        )
        Text(
            text = stringResource(R.string.reports_filter_clients),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        ToggleChipGroup(
            items = state.clients.map { it.id to it.name },
            selectedIds = viewModel.filterClientIds,
            onToggle = viewModel::toggleFilterClient,
        )

        DateRangeRow(
            from = viewModel.rangeFrom,
            to = viewModel.rangeTo,
            onFromChanged = viewModel::onRangeFromChanged,
            onToChanged = viewModel::onRangeToChanged,
            onPreset = viewModel::applyPreset,
            onRefresh = viewModel::refreshReport,
        )

        Button(onClick = viewModel::exportTimeline, modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.timelines_export))
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        LazyColumn(modifier = Modifier.padding(top = 8.dp).weight(1f)) {
            items(state.items) { item ->
                TimelineReportItemRow(item)
                HorizontalDivider()
            }
            item {
                Text(
                    text = stringResource(R.string.timelines_history),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                )
            }
            items(state.timelines, key = { it.id }) { timeline ->
                TimelineRow(
                    timeline = timeline,
                    onDownload = {
                        viewModel.downloadTimeline(timeline.id) { bytes ->
                            shareFile(context, "timeline-${timeline.id}.csv", bytes, "text/csv")
                        }
                    },
                    onEmail = { viewModel.openEmail(timeline) },
                    onDelete = { viewModel.requestDelete(timeline) },
                )
                HorizontalDivider()
            }
        }
    }

    viewModel.emailing?.let {
        EmailTimelineDialog(
            form = viewModel.emailForm,
            onFormChanged = viewModel::updateEmailForm,
            onSend = viewModel::sendEmail,
            onCancel = viewModel::cancelEmail,
        )
    }

    viewModel.pendingDelete?.let { timeline ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text(stringResource(R.string.timer_delete_confirm_title)) },
            text = { Text(stringResource(R.string.timelines_delete_confirm, timeline.id)) },
            confirmButton = {
                Button(onClick = viewModel::confirmDelete) { Text(stringResource(R.string.common_confirm)) }
            },
            dismissButton = {
                Button(onClick = viewModel::cancelDelete) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

@Composable
private fun TimelineReportItemRow(item: ReportItemDto) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(text = item.name?.takeIf { it.isNotBlank() && it != "*" } ?: "—")
        val subtitle = listOfNotNull(item.project, item.client, item.userUid).joinToString(" · ")
        if (subtitle.isNotBlank()) {
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Text(formatDurationHms(item.totalDuration), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TimelineRow(
    timeline: TimelineDto,
    onDownload: () -> Unit,
    onEmail: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = "#${timeline.id} · ${timeline.status}")
        Text(
            text = timeline.timeInterval ?: "",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text =
                formatDurationHms(timeline.totalDuration.toLongOrNull() ?: 0) +
                    " · " + CREATED_AT_FORMATTER.format(Instant.ofEpochSecond(timeline.createdAt)),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(modifier = Modifier.padding(top = 4.dp)) {
            Button(onClick = onDownload) { Text(stringResource(R.string.timelines_download)) }
            Button(onClick = onEmail, modifier = Modifier.padding(start = 8.dp)) {
                Text(stringResource(R.string.timelines_email))
            }
            Button(onClick = onDelete, modifier = Modifier.padding(start = 8.dp)) {
                Text(stringResource(R.string.common_delete))
            }
        }
    }
}

@Composable
private fun EmailTimelineDialog(
    form: EmailForm,
    onFormChanged: ((EmailForm) -> EmailForm) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.timelines_email_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = form.email,
                    onValueChange = { value -> onFormChanged { it.copy(email = value) } },
                    label = { Text(stringResource(R.string.timelines_email_address)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = form.subject,
                    onValueChange = { value -> onFormChanged { it.copy(subject = value) } },
                    label = { Text(stringResource(R.string.timelines_email_subject)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = form.content,
                    onValueChange = { value -> onFormChanged { it.copy(content = value) } },
                    label = { Text(stringResource(R.string.timelines_email_message)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = { Button(onClick = onSend) { Text(stringResource(R.string.timelines_send)) } },
        dismissButton = { Button(onClick = onCancel) { Text(stringResource(R.string.common_cancel)) } },
    )
}
