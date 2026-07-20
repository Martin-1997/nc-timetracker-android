package org.mtier.timetracker.data.api

import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
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
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Maps 1:1 onto OCA\TimeTracker\Controller\AjaxController — the exact same
 * endpoints the Vue web frontend calls (see the timetracker repo). No
 * backend changes were needed: Basic Auth with an app password bypasses
 * CSRF on every one of these routes, verified against a live server
 * (PLAN.md §2).
 *
 * Mutations return the raw [JsonObject] rather than a typed model: several
 * of these endpoints have inconsistent success-response shapes (a legacy
 * quirk of the PHP backend, left untouched on purpose). The repository
 * layer checks [Response.isSuccessful] and always re-fetches the
 * authoritative list afterward rather than trusting an embedded payload —
 * exactly what the Vue frontend does too.
 */
interface TimeTrackerApi {
    // Tags
    @GET("apps/timetracker/ajax/tags")
    suspend fun getTags(): TagsResponse

    @POST("apps/timetracker/ajax/add-tag/{name}")
    suspend fun addTag(
        @Path("name") name: String,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/edit-tag/{id}")
    suspend fun editTag(
        @Path("id") id: Int,
        @Body body: EditNameRequest,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/delete-tag/{id}")
    suspend fun deleteTag(
        @Path("id") id: Int,
    ): Response<JsonObject>

    // Clients
    @GET("apps/timetracker/ajax/clients")
    suspend fun getClients(): ClientsResponse

    @POST("apps/timetracker/ajax/add-client/{name}")
    suspend fun addClient(
        @Path("name") name: String,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/edit-client/{id}")
    suspend fun editClient(
        @Path("id") id: Int,
        @Body body: EditNameRequest,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/delete-client/{id}")
    suspend fun deleteClient(
        @Path("id") id: Int,
    ): Response<JsonObject>

    // Projects
    @GET("apps/timetracker/ajax/projects")
    suspend fun getProjects(): ProjectsResponse

    @GET("apps/timetracker/ajax/projects-table")
    suspend fun getProjectsTable(
        @Query("archived") archived: Int,
    ): ProjectsTableResponse

    @POST("apps/timetracker/ajax/add-project/{name}")
    suspend fun addProject(
        @Path("name") name: String,
        @Body body: AddProjectRequest,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/edit-project/{id}")
    suspend fun editProject(
        @Path("id") id: Int,
        @Body body: EditProjectRequest,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/delete-project-with-data/{id}")
    suspend fun deleteProjectWithData(
        @Path("id") id: Int,
    ): Response<JsonObject>

    // Timer
    @GET("apps/timetracker/ajax/work-intervals")
    suspend fun getWorkIntervals(
        @Query("from") from: Long,
        @Query("to") to: Long,
        @Query("tzoffset") tzOffset: Int,
    ): WorkIntervalsResponse

    /** [name] must already be percent-encoded twice — see ApiNameEncoding.kt. */
    @POST("apps/timetracker/ajax/start-timer/{name}")
    suspend fun startTimer(
        @Path("name", encoded = true) name: String,
        @Body body: StartTimerRequest,
    ): Response<JsonObject>

    /** [name] must already be percent-encoded twice — see ApiNameEncoding.kt. */
    @POST("apps/timetracker/ajax/stop-timer/{name}")
    suspend fun stopTimer(
        @Path("name", encoded = true) name: String,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/update-work-interval/{id}")
    suspend fun updateWorkIntervalNameDetails(
        @Path("id") id: Int,
        @Body body: UpdateNameDetailsRequest,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/update-work-interval/{id}")
    suspend fun updateWorkIntervalProject(
        @Path("id") id: Int,
        @Body body: UpdateProjectRequest,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/update-work-interval/{id}")
    suspend fun updateWorkIntervalTags(
        @Path("id") id: Int,
        @Body body: UpdateTagsRequest,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/update-work-interval/{id}")
    suspend fun updateWorkIntervalTime(
        @Path("id") id: Int,
        @Body body: UpdateTimeRequest,
    ): Response<JsonObject>

    /** [name] must already be percent-encoded twice — see ApiNameEncoding.kt. */
    @POST("apps/timetracker/ajax/add-work-interval/{name}")
    suspend fun addWorkInterval(
        @Path("name", encoded = true) name: String,
        @Body body: AddWorkIntervalRequest,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/delete-work-interval/{id}")
    suspend fun deleteWorkInterval(
        @Path("id") id: Int,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/add-cost/{id}")
    suspend fun addCost(
        @Path("id") id: Int,
        @Body body: AddCostRequest,
    ): Response<JsonObject>

    // Reports / Dashboard / Timelines
    @Suppress("LongParameterList")
    @GET("apps/timetracker/ajax/report")
    suspend fun getReport(
        @Query("name") name: String,
        @Query("from") from: Long,
        @Query("to") to: Long,
        @Query("group1") group1: String,
        @Query("group2") group2: String,
        @Query("timegroup") timegroup: String,
        @Query("filterProjectId") filterProjectId: String,
        @Query("filterClientId") filterClientId: String,
    ): ReportResponse

    @GET("apps/timetracker/ajax/goals")
    suspend fun getGoals(): GoalsResponse

    /**
     * Form-urlencoded rather than a JSON [Body]: AjaxController::addGoal()
     * takes no route parameter, and this backend only merges a JSON POST
     * body into `$this->request` for controller methods that have at
     * least one route/path parameter to resolve — confirmed empirically
     * (a JSON body silently leaves `projectId` null here, failing a
     * NOT NULL constraint, while the identical fields sent form-urlencoded
     * work). postTimeline() below has the same zero-parameter shape.
     */
    @FormUrlEncoded
    @POST("apps/timetracker/ajax/add-goal")
    suspend fun addGoal(
        @Field("projectId") projectId: String,
        @Field("hours") hours: String,
        @Field("interval") interval: String,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/delete-goal/{id}")
    suspend fun deleteGoal(
        @Path("id") id: Int,
    ): Response<JsonObject>

    /** See [addGoal]'s kdoc — same zero-route-parameter form-urlencoded requirement. */
    @Suppress("LongParameterList")
    @FormUrlEncoded
    @POST("apps/timetracker/ajax/timeline")
    suspend fun postTimeline(
        @Field("name") name: String,
        @Field("from") from: Long,
        @Field("to") to: Long,
        @Field("group1") group1: String,
        @Field("group2") group2: String,
        @Field("timegroup") timegroup: String,
        @Field("filterProjectId") filterProjectId: String,
        @Field("filterClientId") filterClientId: String,
    ): Response<JsonObject>

    @GET("apps/timetracker/ajax/timelines")
    suspend fun getTimelines(): TimelinesResponse

    @GET("apps/timetracker/ajax/timelines-admin")
    suspend fun getTimelinesAdmin(): TimelinesResponse

    @POST("apps/timetracker/ajax/edit-timeline/{id}")
    suspend fun editTimeline(
        @Path("id") id: Int,
        @Body body: EditTimelineStatusRequest,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/delete-timeline/{id}")
    suspend fun deleteTimeline(
        @Path("id") id: Int,
    ): Response<JsonObject>

    @POST("apps/timetracker/ajax/email-timeline/{id}")
    suspend fun emailTimeline(
        @Path("id") id: Int,
        @Body body: EmailTimelineRequest,
    ): Response<JsonObject>

    @Streaming
    @GET("apps/timetracker/ajax/download-timeline/{id}")
    suspend fun downloadTimeline(
        @Path("id") id: Int,
    ): ResponseBody
}
