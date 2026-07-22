package org.mtier.timetracker.fakes

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.ResponseBody
import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.AddCostRequest
import org.mtier.timetracker.data.api.dto.AddProjectRequest
import org.mtier.timetracker.data.api.dto.AddWorkIntervalRequest
import org.mtier.timetracker.data.api.dto.ClientsResponse
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
import org.mtier.timetracker.data.api.dto.WorkIntervalsResponse
import retrofit2.Response

/**
 * A hand-written in-memory fake of [TimeTrackerApi] for unit tests — avoids
 * a reflection-based mocking library entirely (MockK's kotlin-reflect
 * bootstrap proved unusably slow, multiple minutes and eventually an OOM,
 * in this project's constrained build environment; a plain fake has none
 * of that cost and is just as valid a test double). Only the methods
 * exercised by the tests that use this fake have real configurable
 * behavior; everything else throws if a test path ever reaches it.
 */
@Suppress("TooManyFunctions")
class FakeTimeTrackerApi : TimeTrackerApi {
    var workIntervalsResponse: WorkIntervalsResponse = WorkIntervalsResponse()
    var workIntervalsError: Throwable? = null
    val getWorkIntervalsCalls = mutableListOf<Triple<Long, Long, Int>>()

    override suspend fun getWorkIntervals(
        from: Long,
        to: Long,
        tzOffset: Int,
    ): WorkIntervalsResponse {
        getWorkIntervalsCalls.add(Triple(from, to, tzOffset))
        workIntervalsError?.let { throw it }
        return workIntervalsResponse
    }

    val startTimerCalls = mutableListOf<Pair<String, StartTimerRequest>>()
    var startTimerErrorMessage: String? = null

    override suspend fun startTimer(
        name: String,
        body: StartTimerRequest,
    ): Response<JsonObject> {
        startTimerCalls.add(name to body)
        val error = startTimerErrorMessage
        val responseBody = if (error != null) JsonObject(mapOf("Error" to JsonPrimitive(error))) else JsonObject(emptyMap())
        return Response.success(responseBody)
    }

    val stopTimerCalls = mutableListOf<String>()

    override suspend fun stopTimer(name: String): Response<JsonObject> {
        stopTimerCalls.add(name)
        return Response.success(JsonObject(emptyMap()))
    }

    var projectsResponse: ProjectsResponse = ProjectsResponse()
    var projectsError: Throwable? = null

    override suspend fun getProjects(): ProjectsResponse {
        projectsError?.let { throw it }
        return projectsResponse
    }

    var tagsResponse: TagsResponse = TagsResponse()
    var tagsError: Throwable? = null

    override suspend fun getTags(): TagsResponse {
        tagsError?.let { throw it }
        return tagsResponse
    }

    var clientsResponse: ClientsResponse = ClientsResponse()
    var clientsError: Throwable? = null

    override suspend fun getClients(): ClientsResponse {
        clientsError?.let { throw it }
        return clientsResponse
    }

    var goalsResponse: GoalsResponse = GoalsResponse()

    override suspend fun getGoals(): GoalsResponse = goalsResponse

    val addGoalCalls = mutableListOf<Triple<String, String, String>>()
    var addGoalErrorMessage: String? = null

    override suspend fun addGoal(
        projectId: String,
        hours: String,
        interval: String,
    ): Response<JsonObject> {
        addGoalCalls.add(Triple(projectId, hours, interval))
        val error = addGoalErrorMessage
        val body = if (error != null) JsonObject(mapOf("Error" to JsonPrimitive(error))) else JsonObject(emptyMap())
        return Response.success(body)
    }

    val deleteGoalCalls = mutableListOf<Int>()

    override suspend fun deleteGoal(id: Int): Response<JsonObject> {
        deleteGoalCalls.add(id)
        return Response.success(JsonObject(emptyMap()))
    }

    private fun notUsedInThisTest(): Nothing = throw UnsupportedOperationException("Not exercised by this unit test")

    override suspend fun addTag(name: String) = notUsedInThisTest()

    override suspend fun editTag(
        id: Int,
        body: EditNameRequest,
    ) = notUsedInThisTest()

    override suspend fun deleteTag(id: Int) = notUsedInThisTest()

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
