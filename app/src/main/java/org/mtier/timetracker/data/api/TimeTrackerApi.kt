package org.mtier.timetracker.data.api

import kotlinx.serialization.json.JsonObject
import org.mtier.timetracker.data.api.dto.AddCostRequest
import org.mtier.timetracker.data.api.dto.AddProjectRequest
import org.mtier.timetracker.data.api.dto.AddWorkIntervalRequest
import org.mtier.timetracker.data.api.dto.ClientsResponse
import org.mtier.timetracker.data.api.dto.EditNameRequest
import org.mtier.timetracker.data.api.dto.EditProjectRequest
import org.mtier.timetracker.data.api.dto.ProjectsResponse
import org.mtier.timetracker.data.api.dto.ProjectsTableResponse
import org.mtier.timetracker.data.api.dto.StartTimerRequest
import org.mtier.timetracker.data.api.dto.TagsResponse
import org.mtier.timetracker.data.api.dto.UpdateNameDetailsRequest
import org.mtier.timetracker.data.api.dto.UpdateProjectRequest
import org.mtier.timetracker.data.api.dto.UpdateTagsRequest
import org.mtier.timetracker.data.api.dto.UpdateTimeRequest
import org.mtier.timetracker.data.api.dto.WorkIntervalsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
    suspend fun addTag(@Path("name") name: String): Response<JsonObject>

    @POST("apps/timetracker/ajax/edit-tag/{id}")
    suspend fun editTag(@Path("id") id: Int, @Body body: EditNameRequest): Response<JsonObject>

    @POST("apps/timetracker/ajax/delete-tag/{id}")
    suspend fun deleteTag(@Path("id") id: Int): Response<JsonObject>

    // Clients
    @GET("apps/timetracker/ajax/clients")
    suspend fun getClients(): ClientsResponse

    @POST("apps/timetracker/ajax/add-client/{name}")
    suspend fun addClient(@Path("name") name: String): Response<JsonObject>

    @POST("apps/timetracker/ajax/edit-client/{id}")
    suspend fun editClient(@Path("id") id: Int, @Body body: EditNameRequest): Response<JsonObject>

    @POST("apps/timetracker/ajax/delete-client/{id}")
    suspend fun deleteClient(@Path("id") id: Int): Response<JsonObject>

    // Projects
    @GET("apps/timetracker/ajax/projects")
    suspend fun getProjects(): ProjectsResponse

    @GET("apps/timetracker/ajax/projects-table")
    suspend fun getProjectsTable(@Query("archived") archived: Int): ProjectsTableResponse

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
    suspend fun deleteProjectWithData(@Path("id") id: Int): Response<JsonObject>

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
    suspend fun stopTimer(@Path("name", encoded = true) name: String): Response<JsonObject>

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
    suspend fun deleteWorkInterval(@Path("id") id: Int): Response<JsonObject>

    @POST("apps/timetracker/ajax/add-cost/{id}")
    suspend fun addCost(@Path("id") id: Int, @Body body: AddCostRequest): Response<JsonObject>
}
