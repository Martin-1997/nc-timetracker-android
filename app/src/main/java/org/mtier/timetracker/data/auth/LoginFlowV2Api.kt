package org.mtier.timetracker.data.auth

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Nextcloud Login Flow v2, implemented directly against the server (no
 * dependency on the Files app / Android-SingleSignOn library — see
 * PLAN.md §3). Protocol: https://docs.nextcloud.com/server/latest/developer_manual/client_apis/LoginFlow/index.html
 */
interface LoginFlowV2Api {

    @POST("index.php/login/v2")
    suspend fun initiate(): LoginFlowInitResponse

    /**
     * [endpoint] is the absolute poll URL returned by [initiate]; Retrofit
     * uses it as-is regardless of the client's base URL. Returns 404 while
     * the user hasn't finished the browser login yet.
     */
    @FormUrlEncoded
    @POST
    suspend fun poll(
        @Url endpoint: String,
        @Field("token") token: String,
    ): Response<LoginFlowPollResponse>
}

@Serializable
data class LoginFlowInitResponse(
    val poll: PollInfo,
    val login: String,
)

@Serializable
data class PollInfo(
    val token: String,
    val endpoint: String,
)

@Serializable
data class LoginFlowPollResponse(
    val server: String,
    val loginName: String,
    val appPassword: String,
)
