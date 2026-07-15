package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.doubleEncodeName
import org.mtier.timetracker.data.api.dto.AddCostRequest
import org.mtier.timetracker.data.api.dto.AddWorkIntervalRequest
import org.mtier.timetracker.data.api.dto.StartTimerRequest
import org.mtier.timetracker.data.api.dto.UpdateNameDetailsRequest
import org.mtier.timetracker.data.api.dto.UpdateProjectRequest
import org.mtier.timetracker.data.api.dto.UpdateTagsRequest
import org.mtier.timetracker.data.api.dto.UpdateTimeRequest
import org.mtier.timetracker.data.api.dto.WorkIntervalsResponse
import org.mtier.timetracker.data.api.throwOnError
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val api: TimeTrackerApi,
) {
    suspend fun getWorkIntervals(from: Instant, to: Instant): WorkIntervalsResponse =
        api.getWorkIntervals(from.epochSecond, to.epochSecond, currentTzOffsetMinutes())

    suspend fun startTimer(name: String, projectId: Int?, tagIds: List<String>) {
        api.startTimer(
            doubleEncodeName(name),
            StartTimerRequest(projectId?.toString() ?: "", tagIds.joinToString(",")),
        ).throwOnError()
    }

    suspend fun stopTimer(name: String) {
        api.stopTimer(doubleEncodeName(name)).throwOnError()
    }

    suspend fun resume(name: String, projectId: Int?, tagIds: List<String>) {
        startTimer(name, projectId, tagIds)
    }

    suspend fun updateNameDetails(id: Int, name: String, details: String) {
        api.updateWorkIntervalNameDetails(id, UpdateNameDetailsRequest(name, details)).throwOnError()
    }

    suspend fun updateProject(id: Int, projectId: Int?) {
        api.updateWorkIntervalProject(id, UpdateProjectRequest(projectId?.toString() ?: "")).throwOnError()
    }

    /** [tagIdsOrNames] entries are either existing numeric tag ids, or a
     *  freshly-typed tag name — the backend creates a new Tag row for any
     *  non-numeric value here (unlike Projects' allowedTags — see
     *  PLAN.md / TimerView.vue history for why that distinction matters). */
    suspend fun updateTags(id: Int, tagIdsOrNames: List<String>) {
        api.updateWorkIntervalTags(id, UpdateTagsRequest(tagIdsOrNames.joinToString(","))).throwOnError()
    }

    suspend fun updateTime(id: Int, start: Instant, end: Instant) {
        api.updateWorkIntervalTime(
            id,
            UpdateTimeRequest(start.toApiDateString(), end.toApiDateString(), currentTzOffsetMinutes()),
        ).throwOnError()
    }

    suspend fun addManualEntry(name: String, details: String, start: Instant, end: Instant) {
        api.addWorkInterval(
            doubleEncodeName(name),
            AddWorkIntervalRequest(
                start = start.toApiDateString(),
                end = end.toApiDateString(),
                tzoffset = currentTzOffsetMinutes(),
                details = details,
            ),
        ).throwOnError()
    }

    suspend fun deleteWorkInterval(id: Int) {
        api.deleteWorkInterval(id).throwOnError()
    }

    suspend fun addCost(id: Int, cost: String) {
        api.addCost(id, AddCostRequest(cost)).throwOnError()
    }
}
