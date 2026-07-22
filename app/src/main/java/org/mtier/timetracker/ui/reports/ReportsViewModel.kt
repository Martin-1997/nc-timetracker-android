package org.mtier.timetracker.ui.reports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.api.dto.ReportItemDto
import org.mtier.timetracker.data.repository.ClientsRepository
import org.mtier.timetracker.data.repository.ProjectsRepository
import org.mtier.timetracker.data.repository.ReportsRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class ReportsUiState(
    val items: List<ReportItemDto> = emptyList(),
    val projects: List<ProjectDto> = emptyList(),
    val clients: List<ClientDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

val REPORT_GROUP_OPTIONS = listOf("", "project", "userUid", "client")
val REPORT_GROUP2_OPTIONS = listOf("", "name", "userUid", "project")
val REPORT_TIMEGROUP_OPTIONS = listOf("", "day", "week", "month", "year")

private const val DEFAULT_RANGE_DAYS = 30L

@HiltViewModel
class ReportsViewModel
    @Inject
    constructor(
        private val reportsRepository: ReportsRepository,
        private val projectsRepository: ProjectsRepository,
        private val clientsRepository: ClientsRepository,
    ) : ViewModel() {
        var uiState by mutableStateOf(ReportsUiState())
            private set

        var rangeFrom by mutableStateOf(Instant.now().minus(DEFAULT_RANGE_DAYS, ChronoUnit.DAYS))
            private set

        var rangeTo by mutableStateOf(Instant.now())
            private set

        var group1 by mutableStateOf("project")
            private set

        var group2 by mutableStateOf("userUid")
            private set

        var timegroup by mutableStateOf("day")
            private set

        var filterProjectIds by mutableStateOf<Set<Int>>(emptySet())
            private set

        var filterClientIds by mutableStateOf<Set<Int>>(emptySet())
            private set

        init {
            refresh()
            viewModelScope.launch {
                runCatching { projectsRepository.getProjects() }
                    .onSuccess { uiState = uiState.copy(projects = it) }
            }
            viewModelScope.launch {
                runCatching { clientsRepository.getClients() }
                    .onSuccess { uiState = uiState.copy(clients = it) }
            }
        }

        fun onRangeFromChanged(value: Instant) {
            rangeFrom = value
            refresh()
        }

        fun onRangeToChanged(value: Instant) {
            rangeTo = value
            refresh()
        }

        fun applyPreset(range: Pair<Instant, Instant>) {
            rangeFrom = range.first
            rangeTo = range.second
            refresh()
        }

        fun onGroup1Changed(value: String) {
            group1 = value
            refresh()
        }

        fun onGroup2Changed(value: String) {
            group2 = value
            refresh()
        }

        fun onTimegroupChanged(value: String) {
            timegroup = value
            refresh()
        }

        fun toggleFilterProject(id: Int) {
            filterProjectIds = if (id in filterProjectIds) filterProjectIds - id else filterProjectIds + id
            refresh()
        }

        fun toggleFilterClient(id: Int) {
            filterClientIds = if (id in filterClientIds) filterClientIds - id else filterClientIds + id
            refresh()
        }

        fun refresh() {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModelScope.launch {
                runCatching {
                    reportsRepository.getReport(
                        from = rangeFrom,
                        to = rangeTo,
                        group1 = group1,
                        group2 = group2,
                        timegroup = timegroup,
                        filterProjectIds = filterProjectIds.toList(),
                        filterClientIds = filterClientIds.toList(),
                    )
                }.onSuccess { uiState = uiState.copy(items = it, isLoading = false) }
                    .onFailure { uiState = uiState.copy(isLoading = false, errorMessage = it.message) }
            }
        }
    }
