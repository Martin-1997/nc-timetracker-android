package org.mtier.timetracker.ui.timer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.api.dto.TagDto
import org.mtier.timetracker.data.api.dto.WorkIntervalItemDto
import org.mtier.timetracker.data.repository.ProjectsRepository
import org.mtier.timetracker.data.repository.TagsRepository
import org.mtier.timetracker.data.repository.TimerRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class CostStatus { SUCCESS, ERROR }

data class DayGroup(
    val label: String,
    val items: List<WorkIntervalItemDto>,
)

data class TimerUiState(
    val days: List<DayGroup> = emptyList(),
    val isRunning: Boolean = false,
    val liveElapsedSeconds: Long = 0,
    val projects: List<ProjectDto> = emptyList(),
    val tags: List<TagDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class EditNameDetailsState(
    val item: WorkIntervalItemDto,
    val name: String,
    val details: String,
)

data class EditTimeState(
    val item: WorkIntervalItemDto,
    val start: Instant,
    val end: Instant,
)

data class ManualEntryState(
    val name: String = "",
    val details: String = "",
    val start: Instant,
    val end: Instant,
)

@HiltViewModel
class TimerViewModel
    @Inject
    constructor(
        private val timerRepository: TimerRepository,
        private val projectsRepository: ProjectsRepository,
        private val tagsRepository: TagsRepository,
    ) : ViewModel() {
        var uiState by mutableStateOf(TimerUiState())
            private set

        var workInput by mutableStateOf("")
            private set

        var rangeFrom by mutableStateOf(Instant.now().minus(DEFAULT_RANGE_DAYS, ChronoUnit.DAYS))
            private set

        var rangeTo by mutableStateOf(Instant.now())
            private set

        var costDrafts by mutableStateOf<Map<Int, String>>(emptyMap())
            private set

        var costStatus by mutableStateOf<Map<Int, CostStatus>>(emptyMap())
            private set

        var editingNameDetails by mutableStateOf<EditNameDetailsState?>(null)
            private set

        var editingTime by mutableStateOf<EditTimeState?>(null)
            private set

        var manualEntry by mutableStateOf<ManualEntryState?>(null)
            private set

        var pendingDelete by mutableStateOf<WorkIntervalItemDto?>(null)
            private set

        private var runningStartEpochSecond: Long? = null
        private var tickerJob: Job? = null

        init {
            refresh()
            viewModelScope.launch {
                runCatching { projectsRepository.getProjects() }
                    .onSuccess { uiState = uiState.copy(projects = it) }
            }
            viewModelScope.launch {
                runCatching { tagsRepository.getTags() }
                    .onSuccess { uiState = uiState.copy(tags = it) }
            }
        }

        fun onWorkInputChanged(value: String) {
            workInput = value
        }

        fun applyPreset(range: Pair<Instant, Instant>) {
            rangeFrom = range.first
            rangeTo = range.second
            refresh()
        }

        fun onRangeFromChanged(value: Instant) {
            rangeFrom = value
        }

        fun onRangeToChanged(value: Instant) {
            rangeTo = value
        }

        fun refresh() {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModelScope.launch {
                runCatching { timerRepository.getWorkIntervals(rangeFrom, rangeTo) }
                    .onSuccess { response ->
                        val days =
                            response.days.map { (label, nameGroups) ->
                                DayGroup(
                                    label = label,
                                    items =
                                        nameGroups.values
                                            .flatMap { it.children }
                                            .sortedByDescending { it.start },
                                )
                            }
                        val running = response.running
                        uiState =
                            uiState.copy(
                                days = days,
                                isRunning = running.isNotEmpty(),
                                isLoading = false,
                            )
                        if (running.isNotEmpty()) {
                            startTicker(running.first().start)
                        } else {
                            stopTicker()
                        }
                    }.onFailure { uiState = uiState.copy(isLoading = false, errorMessage = it.message) }
            }
        }

        private fun startTicker(startEpochSecond: Long) {
            runningStartEpochSecond = startEpochSecond
            tickerJob?.cancel()
            tickerJob =
                viewModelScope.launch {
                    while (true) {
                        val start = runningStartEpochSecond ?: break
                        uiState = uiState.copy(liveElapsedSeconds = Instant.now().epochSecond - start)
                        delay(TICKER_INTERVAL_MS)
                    }
                }
        }

        private fun stopTicker() {
            tickerJob?.cancel()
            tickerJob = null
            runningStartEpochSecond = null
            uiState = uiState.copy(liveElapsedSeconds = 0)
        }

        fun onSubmitWorkInput() {
            if (uiState.isRunning) {
                stopTimer()
            } else {
                startTimer(workInput.trim().ifEmpty { "no description" }, null, emptyList())
            }
        }

        private fun startTimer(
            name: String,
            projectId: Int?,
            tagIds: List<String>,
        ) {
            viewModelScope.launch {
                runCatching { timerRepository.startTimer(name, projectId, tagIds) }
                    .onSuccess {
                        workInput = ""
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        private fun stopTimer() {
            viewModelScope.launch {
                runCatching { timerRepository.stopTimer(workInput.trim().ifEmpty { "no description" }) }
                    .onSuccess {
                        workInput = ""
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun resume(item: WorkIntervalItemDto) {
            viewModelScope.launch {
                runCatching {
                    if (uiState.isRunning) {
                        timerRepository.stopTimer("no description")
                    }
                    timerRepository.resume(item.name, item.projectId, item.tags.map { it.id.toString() })
                }.onSuccess { refresh() }
                    .onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun openEditNameDetails(item: WorkIntervalItemDto) {
            editingNameDetails = EditNameDetailsState(item, item.name, item.details ?: "")
        }

        fun updateEditingNameDetails(transform: (EditNameDetailsState) -> EditNameDetailsState) {
            editingNameDetails = editingNameDetails?.let(transform)
        }

        fun cancelEditingNameDetails() {
            editingNameDetails = null
        }

        fun saveNameDetails() {
            val state = editingNameDetails ?: return
            viewModelScope.launch {
                runCatching { timerRepository.updateNameDetails(state.item.id, state.name, state.details) }
                    .onSuccess {
                        editingNameDetails = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun updateProject(
            item: WorkIntervalItemDto,
            projectId: Int?,
        ) {
            viewModelScope.launch {
                runCatching { timerRepository.updateProject(item.id, projectId) }
                    .onSuccess { refresh() }
                    .onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun updateTags(
            item: WorkIntervalItemDto,
            tagIdsOrNames: List<String>,
        ) {
            viewModelScope.launch {
                runCatching { timerRepository.updateTags(item.id, tagIdsOrNames) }
                    .onSuccess { refresh() }
                    .onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun openEditTime(item: WorkIntervalItemDto) {
            editingTime =
                EditTimeState(
                    item = item,
                    start = Instant.ofEpochSecond(item.start),
                    end = Instant.ofEpochSecond(item.start + item.duration),
                )
        }

        fun updateEditingTime(transform: (EditTimeState) -> EditTimeState) {
            editingTime = editingTime?.let(transform)
        }

        fun cancelEditingTime() {
            editingTime = null
        }

        fun saveEditingTime() {
            val state = editingTime ?: return
            viewModelScope.launch {
                runCatching { timerRepository.updateTime(state.item.id, state.start, state.end) }
                    .onSuccess {
                        editingTime = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun openManualEntry() {
            val now = Instant.now()
            manualEntry = ManualEntryState(start = now, end = now)
        }

        fun updateManualEntry(transform: (ManualEntryState) -> ManualEntryState) {
            manualEntry = manualEntry?.let(transform)
        }

        fun cancelManualEntry() {
            manualEntry = null
        }

        fun saveManualEntry() {
            val state = manualEntry ?: return
            viewModelScope.launch {
                runCatching {
                    timerRepository.addManualEntry(state.name, state.details, state.start, state.end)
                }.onSuccess {
                    manualEntry = null
                    refresh()
                }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }

        fun costDraft(item: WorkIntervalItemDto): String = costDrafts[item.id] ?: formatCents(item.cost ?: 0)

        fun onCostDraftChanged(
            item: WorkIntervalItemDto,
            value: String,
        ) {
            costDrafts = costDrafts + (item.id to value)
        }

        fun commitCost(item: WorkIntervalItemDto) {
            val value = costDrafts[item.id] ?: return
            viewModelScope.launch {
                runCatching { timerRepository.addCost(item.id, value) }
                    .onSuccess {
                        costStatus = costStatus + (item.id to CostStatus.SUCCESS)
                        refresh()
                        delay(COST_FEEDBACK_FLASH_MS)
                        costStatus = costStatus - item.id
                    }.onFailure {
                        costStatus = costStatus + (item.id to CostStatus.ERROR)
                        uiState = uiState.copy(errorMessage = it.message)
                    }
            }
        }

        fun requestDelete(item: WorkIntervalItemDto) {
            pendingDelete = item
        }

        fun cancelDelete() {
            pendingDelete = null
        }

        fun confirmDelete() {
            val item = pendingDelete ?: return
            viewModelScope.launch {
                runCatching { timerRepository.deleteWorkInterval(item.id) }
                    .onSuccess {
                        pendingDelete = null
                        refresh()
                    }.onFailure { uiState = uiState.copy(errorMessage = it.message) }
            }
        }
    }

private const val DEFAULT_RANGE_DAYS = 30L
private const val TICKER_INTERVAL_MS = 1_000L
private const val COST_FEEDBACK_FLASH_MS = 3_000L
private const val CENTS_PER_UNIT = 100.0

private fun formatCents(cents: Int): String = "%.2f".format(cents / CENTS_PER_UNIT)
