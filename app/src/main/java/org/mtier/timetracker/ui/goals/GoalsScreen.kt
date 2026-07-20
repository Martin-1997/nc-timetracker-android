package org.mtier.timetracker.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.mtier.timetracker.R
import org.mtier.timetracker.data.api.dto.GoalDto
import org.mtier.timetracker.data.api.dto.ProjectDto

@Composable
fun GoalsScreen(
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        NewGoalForm(
            projects = state.projects,
            selectedProjectId = viewModel.newGoalProjectId,
            onProjectChanged = viewModel::onNewGoalProjectChanged,
            hours = viewModel.newGoalHours,
            onHoursChanged = viewModel::onNewGoalHoursChanged,
            interval = viewModel.newGoalInterval,
            onIntervalChanged = viewModel::onNewGoalIntervalChanged,
            onAdd = viewModel::addGoal,
        )

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        if (state.isLoading && state.goals.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        } else {
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(state.goals, key = { it.id }) { goal ->
                    GoalRow(goal, onDelete = { viewModel.requestDelete(goal) })
                    HorizontalDivider()
                }
            }
        }
    }

    viewModel.pendingDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text(stringResource(R.string.timer_delete_confirm_title)) },
            text = { Text(goal.projectName ?: "") },
            confirmButton = {
                Button(onClick = viewModel::confirmDelete) { Text(stringResource(R.string.common_confirm)) }
            },
            dismissButton = {
                Button(onClick = viewModel::cancelDelete) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun NewGoalForm(
    projects: List<ProjectDto>,
    selectedProjectId: Int?,
    onProjectChanged: (Int?) -> Unit,
    hours: String,
    onHoursChanged: (String) -> Unit,
    interval: String,
    onIntervalChanged: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GoalProjectDropdown(
                projects = projects,
                selectedProjectId = selectedProjectId,
                onProjectChanged = onProjectChanged,
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = hours,
            onValueChange = onHoursChanged,
            label = { Text(stringResource(R.string.goals_hours)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        GoalIntervalDropdown(
            interval = interval,
            onIntervalChanged = onIntervalChanged,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(stringResource(R.string.goals_add))
        }
    }
}

@Composable
private fun GoalProjectDropdown(
    projects: List<ProjectDto>,
    selectedProjectId: Int?,
    onProjectChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName =
        projects.firstOrNull { it.id == selectedProjectId }?.name ?: stringResource(R.string.timer_project)

    Column(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedName)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        onProjectChanged(project.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun GoalIntervalDropdown(
    interval: String,
    onIntervalChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(interval)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GOAL_INTERVALS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onIntervalChanged(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun GoalRow(
    goal: GoalDto,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = goal.projectName ?: stringResource(R.string.timer_project))
            Text(
                text =
                    stringResource(
                        R.string.goals_summary,
                        goal.hours,
                        goal.interval,
                        goal.workedHoursCurrentPeriod,
                        goal.remainingHours,
                    ),
                style = MaterialTheme.typography.bodySmall,
            )
            if (goal.debtHours > 0) {
                Text(
                    text = stringResource(R.string.goals_debt, goal.debtHours, goal.totalRemainingHours),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        Text(
            text = stringResource(R.string.common_delete),
            color = MaterialTheme.colorScheme.error,
            modifier =
                Modifier
                    .clickable(onClick = onDelete)
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
