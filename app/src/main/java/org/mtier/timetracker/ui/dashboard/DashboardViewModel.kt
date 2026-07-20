package org.mtier.timetracker.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.api.dto.ReportItemDto
import org.mtier.timetracker.data.repository.ReportsRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class DashboardSlice(
    val label: String,
    val durationSeconds: Long,
    val costCents: Int,
)

data class DashboardUiState(
    val slices: List<DashboardSlice> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val totalDuration: Long get() = slices.sumOf { it.durationSeconds }
    val totalCost: Int get() = slices.sumOf { it.costCents }
}

private const val DEFAULT_RANGE_DAYS = 29L
private const val NOT_SET_LABEL = "Not set"

@HiltViewModel
class DashboardViewModel
    @Inject
    constructor(
        private val reportsRepository: ReportsRepository,
    ) : ViewModel() {
        var uiState by mutableStateOf(DashboardUiState())
            private set

        var rangeFrom by mutableStateOf(Instant.now().minus(DEFAULT_RANGE_DAYS, ChronoUnit.DAYS))
            private set

        var rangeTo by mutableStateOf(Instant.now())
            private set

        init {
            refresh()
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

        fun refresh() {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            viewModelScope.launch {
                runCatching {
                    reportsRepository.getReport(
                        from = rangeFrom,
                        to = rangeTo,
                        group1 = "client",
                        group2 = "project",
                        timegroup = "",
                        filterProjectIds = emptyList(),
                        filterClientIds = emptyList(),
                    )
                }.onSuccess { items -> uiState = uiState.copy(slices = groupByClientProject(items), isLoading = false) }
                    .onFailure { uiState = uiState.copy(isLoading = false, errorMessage = it.message) }
            }
        }

        private fun groupByClientProject(items: List<ReportItemDto>): List<DashboardSlice> =
            items
                .groupBy { "${it.client ?: NOT_SET_LABEL} / ${it.project ?: NOT_SET_LABEL}" }
                .map { (label, group) ->
                    DashboardSlice(
                        label = label,
                        durationSeconds = group.sumOf { it.totalDuration },
                        costCents = group.sumOf { it.cost ?: 0 },
                    )
                }
    }
