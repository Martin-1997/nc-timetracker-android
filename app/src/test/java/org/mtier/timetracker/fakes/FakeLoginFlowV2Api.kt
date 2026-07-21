package org.mtier.timetracker.fakes

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.mtier.timetracker.data.auth.LoginFlowInitResponse
import org.mtier.timetracker.data.auth.LoginFlowPollResponse
import org.mtier.timetracker.data.auth.LoginFlowV2Api
import org.mtier.timetracker.data.auth.PollInfo
import retrofit2.Response

/** Drives [org.mtier.timetracker.data.auth.AuthRepository.awaitCompletion]'s
 *  real polling loop in a unit test: [pollResultsInOrder] is consumed one
 *  response per call to [poll], so a test can simulate "not done yet" (404)
 *  followed by a successful completion, without any real network or delay. */
class FakeLoginFlowV2Api(
    private val pollResultsInOrder: MutableList<Response<LoginFlowPollResponse>> = mutableListOf(),
) : LoginFlowV2Api {
    var pollCallCount = 0
        private set

    /** When set, every call to [poll] throws this instead of consuming
     *  [pollResultsInOrder] — simulates a network failure mid-poll. */
    var pollError: Throwable? = null

    override suspend fun initiate(): LoginFlowInitResponse =
        LoginFlowInitResponse(
            poll = PollInfo(token = "poll-token", endpoint = "https://cloud.example.com/login/v2/poll"),
            login = "https://cloud.example.com/login/v2/flow/abc",
        )

    override suspend fun poll(
        endpoint: String,
        token: String,
    ): Response<LoginFlowPollResponse> {
        pollCallCount++
        pollError?.let { throw it }
        check(pollResultsInOrder.isNotEmpty()) { "FakeLoginFlowV2Api.poll() called more times than results were queued" }
        return pollResultsInOrder.removeAt(0)
    }

    companion object {
        fun notDoneYet(): Response<LoginFlowPollResponse> =
            Response.error(HTTP_NOT_FOUND, "".toResponseBody("application/json".toMediaType()))

        fun success(
            server: String,
            loginName: String,
            appPassword: String,
        ): Response<LoginFlowPollResponse> = Response.success(LoginFlowPollResponse(server, loginName, appPassword))

        private const val HTTP_NOT_FOUND = 404
    }
}
