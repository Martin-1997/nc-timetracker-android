package org.mtier.timetracker.fakes

import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.AddCostRequest
import org.mtier.timetracker.data.api.dto.AddProjectRequest
import org.mtier.timetracker.data.api.dto.AddWorkIntervalRequest
import org.mtier.timetracker.data.api.dto.ClientsResponse
import org.mtier.timetracker.data.api.dto.DayGroupDto
import org.mtier.timetracker.data.api.dto.EditNameRequest
import org.mtier.timetracker.data.api.dto.EditProjectRequest
import org.mtier.timetracker.data.api.dto.EditTimelineStatusRequest
import org.mtier.timetracker.data.api.dto.EmailTimelineRequest
import org.mtier.timetracker.data.api.dto.GoalsResponse
import org.mtier.timetracker.data.api.dto.ProjectsResponse
import org.mtier.timetracker.data.api.dto.ProjectsTableResponse
import org.mtier.timetracker.data.api.dto.ReportResponse
import org.mtier.timetracker.data.api.dto.StartTimerRequest
import org.mtier.timetracker.data.api.dto.TagsResponse
import org.mtier.timetracker.data.api.dto.TimelinesResponse
import org.mtier.timetracker.data.api.dto.UpdateNameDetailsRequest
import org.mtier.timetracker.data.api.dto.UpdateProjectRequest
import org.mtier.timetracker.data.api.dto.UpdateTagsRequest
import org.mtier.timetracker.data.api.dto.UpdateTimeRequest
import org.mtier.timetracker.data.api.dto.WorkIntervalItemDto
import org.mtier.timetracker.data.api.dto.WorkIntervalsResponse
import retrofit2.Response
import java.net.URLDecoder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * A hand-written in-memory fake of the whole [TimeTrackerApi] surface, used
 * to drive the real production ViewModel/repository/Compose code in an
 * instrumented UI test without a real server. Only the Timer-critical-path
 * methods (getWorkIntervals/startTimer/stopTimer/getProjects/getTags) have
 * real behavior; anything else throws if a test path ever reaches it, since
 * nothing here should call them.
 */
class FakeTimeTrackerApi : TimeTrackerApi {
    private var running: WorkIntervalItemDto? = null
    private val completed = mutableListOf<WorkIntervalItemDto>()
    private var nextId = 1

    private fun decodedName(doubleEncoded: String): String = URLDecoder.decode(URLDecoder.decode(doubleEncoded, "UTF-8"), "UTF-8")

    override suspend fun getWorkIntervals(
        from: Long,
        to: Long,
        tzOffset: Int,
    ): WorkIntervalsResponse {
        val dayLabel = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault()).format(Instant.now())
        val days =
            if (completed.isEmpty()) {
                emptyMap()
            } else {
                mapOf(dayLabel to completed.groupBy { it.name }.mapValues { (_, items) -> DayGroupDto(children = items) })
            }
        return WorkIntervalsResponse(
            running = running?.let { listOf(toRunningDto(it)) } ?: emptyList(),
            days = days,
        )
    }

    override suspend fun startTimer(
        name: String,
        body: StartTimerRequest,
    ): Response<JsonObject> {
        running =
            WorkIntervalItemDto(
                id = nextId++,
                name = decodedName(name),
                projectId = body.projectId.toIntOrNull(),
                start = Instant.now().epochSecond,
                running = 1,
            )
        return Response.success(JsonObject(emptyMap()))
    }

    override suspend fun stopTimer(name: String): Response<JsonObject> {
        running?.let { entry ->
            val duration = (Instant.now().epochSecond - entry.start).toInt()
            completed.add(0, entry.copy(running = 0, duration = duration))
        }
        running = null
        return Response.success(JsonObject(emptyMap()))
    }

    override suspend fun getProjects(): ProjectsResponse = ProjectsResponse()

    override suspend fun getTags(): TagsResponse = TagsResponse()

    private fun toRunningDto(item: WorkIntervalItemDto) =
        org.mtier.timetracker.data.api.dto.RunningIntervalDto(
            id = item.id,
            name = item.name,
            projectId = item.projectId,
            start = item.start,
            running = 1,
        )

    private fun notUsedInThisTest(): Nothing = throw UnsupportedOperationException("Not exercised by this instrumented test")

    override suspend fun addTag(name: String) = notUsedInThisTest()

    override suspend fun editTag(
        id: Int,
        body: EditNameRequest,
    ) = notUsedInThisTest()

    override suspend fun deleteTag(id: Int) = notUsedInThisTest()

    override suspend fun getClients(): ClientsResponse = notUsedInThisTest()

    override suspend fun addClient(name: String) = notUsedInThisTest()

    override suspend fun editClient(
        id: Int,
        body: EditNameRequest,
    ) = notUsedInThisTest()

    override suspend fun deleteClient(id: Int) = notUsedInThisTest()

    override suspend fun getProjectsTable(archived: Int): ProjectsTableResponse = notUsedInThisTest()

    override suspend fun addProject(
        name: String,
        body: AddProjectRequest,
    ) = notUsedInThisTest()

    override suspend fun editProject(
        id: Int,
        body: EditProjectRequest,
    ) = notUsedInThisTest()

    override suspend fun deleteProjectWithData(id: Int) = notUsedInThisTest()

    override suspend fun updateWorkIntervalNameDetails(
        id: Int,
        body: UpdateNameDetailsRequest,
    ) = notUsedInThisTest()

    override suspend fun updateWorkIntervalProject(
        id: Int,
        body: UpdateProjectRequest,
    ) = notUsedInThisTest()

    override suspend fun updateWorkIntervalTags(
        id: Int,
        body: UpdateTagsRequest,
    ) = notUsedInThisTest()

    override suspend fun updateWorkIntervalTime(
        id: Int,
        body: UpdateTimeRequest,
    ) = notUsedInThisTest()

    override suspend fun addWorkInterval(
        name: String,
        body: AddWorkIntervalRequest,
    ) = notUsedInThisTest()

    override suspend fun deleteWorkInterval(id: Int) = notUsedInThisTest()

    override suspend fun addCost(
        id: Int,
        body: AddCostRequest,
    ) = notUsedInThisTest()

    @Suppress("LongParameterList")
    override suspend fun getReport(
        name: String,
        from: Long,
        to: Long,
        group1: String,
        group2: String,
        timegroup: String,
        filterProjectId: String,
        filterClientId: String,
    ): ReportResponse = notUsedInThisTest()

    override suspend fun getGoals(): GoalsResponse = notUsedInThisTest()

    override suspend fun addGoal(
        projectId: String,
        hours: String,
        interval: String,
    ) = notUsedInThisTest()

    override suspend fun deleteGoal(id: Int) = notUsedInThisTest()

    @Suppress("LongParameterList")
    override suspend fun postTimeline(
        name: String,
        from: Long,
        to: Long,
        group1: String,
        group2: String,
        timegroup: String,
        filterProjectId: String,
        filterClientId: String,
    ) = notUsedInThisTest()

    override suspend fun getTimelines(): TimelinesResponse = notUsedInThisTest()

    override suspend fun getTimelinesAdmin(): TimelinesResponse = notUsedInThisTest()

    override suspend fun editTimeline(
        id: Int,
        body: EditTimelineStatusRequest,
    ) = notUsedInThisTest()

    override suspend fun deleteTimeline(id: Int) = notUsedInThisTest()

    override suspend fun emailTimeline(
        id: Int,
        body: EmailTimelineRequest,
    ) = notUsedInThisTest()

    override suspend fun downloadTimeline(id: Int): ResponseBody = notUsedInThisTest()
}
