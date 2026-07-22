package org.mtier.timetracker.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.mtier.timetracker.R
import org.mtier.timetracker.ui.common.DateRangeRow
import org.mtier.timetracker.ui.common.formatCents
import org.mtier.timetracker.ui.theme.parseHexColor

private val SLICE_COLORS =
    listOf(
        "#0082c9", "#e9322d", "#f1c40f", "#27ae60", "#8e44ad",
        "#e67e22", "#16a085", "#2c3e50", "#c0392b", "#2980b9",
    ).map { parseHexColor(it) }

private const val DONUT_STROKE_FRACTION = 0.3f

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
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

        if (state.isLoading && state.slices.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        } else {
            Text(
                text = stringResource(R.string.dashboard_total_time, formatHoursMinutes(state.totalDuration)),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = stringResource(R.string.dashboard_total_costs, formatCents(state.totalCost)),
                style = MaterialTheme.typography.titleMedium,
            )

            DashboardChart(
                title = stringResource(R.string.dashboard_time_by),
                slices = state.slices,
                valueOf = { it.durationSeconds },
                formatValue = { formatHoursMinutes(it) },
            )
            DashboardChart(
                title = stringResource(R.string.dashboard_cost_by),
                slices = state.slices,
                valueOf = { it.costCents.toLong() },
                formatValue = { formatCents(it.toInt()) },
            )
        }
    }
}

@Composable
private fun DashboardChart(
    title: String,
    slices: List<DashboardSlice>,
    valueOf: (DashboardSlice) -> Long,
    formatValue: (Long) -> String,
) {
    val total = slices.sumOf(valueOf)

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        if (total <= 0) {
            Text(
                text = stringResource(R.string.dashboard_no_data),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            return@Column
        }

        Row(modifier = Modifier.padding(top = 12.dp)) {
            DonutCanvas(
                values = slices.map { valueOf(it).toFloat() },
                modifier = Modifier.size(140.dp),
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                slices.forEachIndexed { index, slice ->
                    if (valueOf(slice) <= 0) return@forEachIndexed
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Box(
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .background(SLICE_COLORS[index % SLICE_COLORS.size], CircleShape),
                        )
                        Text(
                            text = "${slice.label}: ${formatValue(valueOf(slice))}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutCanvas(
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    val total = values.sum()
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * DONUT_STROKE_FRACTION
        var startAngle = START_ANGLE_TOP
        values.forEachIndexed { index, value ->
            if (value <= 0f || total <= 0f) return@forEachIndexed
            val sweep = (value / total) * FULL_CIRCLE_DEGREES
            drawArc(
                color = SLICE_COLORS[index % SLICE_COLORS.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth),
            )
            startAngle += sweep
        }
    }
}

private const val FULL_CIRCLE_DEGREES = 360f
private const val START_ANGLE_TOP = -90f
private const val MINUTES_PER_HOUR = 60L
private const val SECONDS_PER_MINUTE = 60L

private fun formatHoursMinutes(totalSeconds: Long): String {
    val h = totalSeconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR)
    val m = (totalSeconds % (SECONDS_PER_MINUTE * MINUTES_PER_HOUR)) / SECONDS_PER_MINUTE
    return "${h}h ${m}m"
}
