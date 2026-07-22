package org.mtier.timetracker.ui.reports

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.mtier.timetracker.R
import org.mtier.timetracker.data.api.dto.ReportItemDto
import org.mtier.timetracker.ui.common.DateRangeRow
import org.mtier.timetracker.ui.common.KeyDropdown
import org.mtier.timetracker.ui.common.ToggleChipGroup
import org.mtier.timetracker.ui.common.formatCents
import org.mtier.timetracker.ui.common.formatDurationHms
import org.mtier.timetracker.ui.common.shareFile

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row {
            KeyDropdown(
                selected = viewModel.group1,
                options = REPORT_GROUP_OPTIONS,
                labelRes = R.string.reports_group_by,
                onSelected = viewModel::onGroup1Changed,
                modifier = Modifier.weight(1f),
            )
            KeyDropdown(
                selected = viewModel.group2,
                options = REPORT_GROUP2_OPTIONS,
                labelRes = R.string.reports_then_by,
                onSelected = viewModel::onGroup2Changed,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
        }
        KeyDropdown(
            selected = viewModel.timegroup,
            options = REPORT_TIMEGROUP_OPTIONS,
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
            onRefresh = viewModel::refresh,
        )

        Row(modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = { shareCsv(context, state.items) }) {
                Text(stringResource(R.string.reports_export_csv))
            }
            Button(onClick = { shareJson(context, state.items) }, modifier = Modifier.padding(start = 8.dp)) {
                Text(stringResource(R.string.reports_export_json))
            }
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        if (state.isLoading && state.items.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        } else {
            ReportTotalsRow(state.items)
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(state.items) { item ->
                    ReportItemRow(item)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ReportTotalsRow(items: List<ReportItemDto>) {
    val totalDuration = items.sumOf { it.totalDuration }
    val totalCost = items.sumOf { it.cost ?: 0 }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(
            text = stringResource(R.string.reports_totals),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
        )
        Text(formatDurationHms(totalDuration), style = MaterialTheme.typography.titleSmall)
        Text(formatCents(totalCost), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun ReportItemRow(item: ReportItemDto) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = item.name?.takeIf { it.isNotBlank() && it != "*" } ?: "—")
        val subtitle =
            listOfNotNull(item.project, item.client, item.userUid, item.ftime)
                .joinToString(" · ")
        if (subtitle.isNotBlank()) {
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
            Text(formatDurationHms(item.totalDuration), style = MaterialTheme.typography.bodySmall)
            item.cost?.let {
                Text(
                    text = formatCents(it),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

private fun shareCsv(
    context: Context,
    items: List<ReportItemDto>,
) {
    val header = listOf("Name", "Project", "Client", "User", "When", "Duration", "Cost")
    val rows =
        items.map { i ->
            listOf(
                i.name ?: "",
                i.project ?: "",
                i.client ?: "",
                i.userUid ?: "",
                i.ftime ?: "",
                formatDurationHms(i.totalDuration),
                formatCents(i.cost ?: 0),
            )
        }
    val csv =
        (listOf(header) + rows)
            .joinToString("\n") { row -> row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" } }
    shareFile(context, "report.csv", csv.toByteArray(), "text/csv")
}

private fun shareJson(
    context: Context,
    items: List<ReportItemDto>,
) {
    val json = Json { prettyPrint = true }
    val text = json.encodeToString(ListSerializer(ReportItemDto.serializer()), items)
    shareFile(context, "report.json", text.toByteArray(), "application/json")
}
