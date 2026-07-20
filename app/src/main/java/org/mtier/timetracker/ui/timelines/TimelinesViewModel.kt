package org.mtier.timetracker.ui.timelines

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
import org.mtier.timetracker.data.api.dto.TimelineDto
import org.mtier.timetracker.data.repository.ClientsRepository
import org.mtier.timetracker.data.repository.ProjectsRepository
import org.mtier.timetracker.data.repository.ReportsRepository
import org.mtier.timetracker.data.repository.TimelinesRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class EmailForm(
    val email: String = "",
    val subject: String = "",
    val content: String = "",
)

data class TimelinesUiState(
    val items: List<ReportItemDto> = emptyList(),
    val timelines: List<TimelineDto> = emptyList(),
    val projects: List<ProjectDto> = emptyList(),
    val clients: List<ClientDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

private const val DEFAULT_RANGE_DAYS = 29L

@HiltViewModel
class TimelinesViewModel
    @Inject
    constructor(
        private val timelinesRepository: TimelinesRepository,
        private val reportsRepository: ReportsRepository,
        private val projectsRepository: ProjectsRepository,
        private val clientsRepository: ClientsRepository,
    ) : ViewModel() {
        var uiState by mutableStateOf(TimelinesUiState())
            private set

        var rangeFrom by mutableStateOf(Instant.now().minus(DEFAULT_RANGE_DAYS, ChronoUnit.DAYS))
            private set

        var rangeTo by mutableStateOf(Instant.now())
            private set

        var group1 by mutableStateOf("project")
            private set

        var group2 by mutableStateOf("")
            private set

        var timegroup by mutableStateOf("day")
            private set

        var filterProjectIds by mutableStateOf<Set<Int>>(emptySet())
            private set

        var filterClientIds by mutableStateOf<Set<Int>>(emptySet())
            private set

        var emailing by mutableStateOf<TimelineDto?>(null)
            private set

        var emailForm by mutableStateOf(EmailForm())
            private set

        var pendingDelete by mutableStateOf<TimelineDto?>(null)
            private set

        init {
            refreshReport()
            loadTimelines()
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
            refreshReport()
        }

        fun onRangeToChanged(value: Instant) {
            rangeTo = value
            refreshReport()
        }

        fun applyPreset(range: Pair<Instant, Instant>) {
            rangeFrom = range.first
            rangeTo = range.second
            refreshReport()
        }

        fun onGroup1Changed(value: String) {
            group1 = value
            refreshReport()
        }

        fun onGroup2Changed(value: String) {
            group2 = value
            refreshReport()
        }

        fun onTimegroupChanged(value: String) {
            timegroup = value
            refreshReport()
        }

        fun toggleFilterProject(id: Int) {
            filterProjectIds = if (id in filterProjectIds) filterProjectIds - id else filterProjectIds + id
            refreshReport()
        }

        fun toggleFilterClient(id: Int) {
            filterClientIds = if (id in filterClientIds) filterClientIds - id else filterClientIds + id
            refreshReport()
        }

        fun refreshReport() {
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

        private fun loadTimelines() {
            viewModelScope.launch {
                runCatching { timelinesRepository.getTimelines() }
                    .onSuccess { uiState = uiState.copy(timelines = it) }
            }
        }

        fun exportTimeline() {
            viewModelScope.launch {
                runCatching {
                    timelinesRepository.exportTimeline(
                        from = rangeFrom,
                        to = rangeTo,
                        group1 = group1,
                        group2 = group2,
                        timegroup = timegroup,
                        filterProjectIds = filterProjectIds.toList(),
                        filterClientIds = filterClientIds.toList(),
                    )
                }.onSuccess { loadTimelines() }
                    .onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun downloadTimeline(
            id: Int,
            onReady: (ByteArray) -> Unit,
        ) {
            viewModelScope.launch {
                runCatching { timelinesRepository.downloadTimelineCsv(id) }
                    .onSuccess(onReady)
                    .onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun openEmail(timeline: TimelineDto) {
            emailing = timeline
            emailForm = EmailForm(subject = "Timeline #${timeline.id}")
        }

        fun updateEmailForm(transform: (EmailForm) -> EmailForm) {
            emailForm = transform(emailForm)
        }

        fun cancelEmail() {
            emailing = null
        }

        fun sendEmail() {
            val timeline = emailing ?: return
            viewModelScope.launch {
                runCatching {
                    timelinesRepository.emailTimeline(timeline.id, emailForm.email, emailForm.subject, emailForm.content)
                }.onSuccess { emailing = null }
                    .onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun requestDelete(timeline: TimelineDto) {
            pendingDelete = timeline
        }

        fun cancelDelete() {
            pendingDelete = null
        }

        fun confirmDelete() {
            val timeline = pendingDelete ?: return
            viewModelScope.launch {
                runCatching { timelinesRepository.deleteTimeline(timeline.id) }
                    .onSuccess {
                        pendingDelete = null
                        loadTimelines()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }
    }
