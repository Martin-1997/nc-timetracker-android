package org.mtier.timetracker.data.auth

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mtier.timetracker.fakes.FakeCredentialStore
import org.mtier.timetracker.fakes.FakeLoginFlowV2Api

/** Covers AuthRepository.awaitCompletion's actual polling loop (PLAN.md §7
 *  names Login Flow v2 polling as a test target) by constructing a
 *  [LoginFlowSession] directly against a [FakeLoginFlowV2Api] — bypassing
 *  beginLoginFlow(), which builds its own Retrofit client per server URL
 *  and so isn't itself unit-testable without a real HTTP call. */
class AuthRepositoryTest {
    private val credentialStore = FakeCredentialStore()
    private val repository = AuthRepository(credentialStore)

    private fun session(api: FakeLoginFlowV2Api) =
        LoginFlowSession(
            serverUrl = "https://cloud.example.com",
            api = api,
            pollToken = "poll-token",
            pollEndpoint = "https://cloud.example.com/login/v2/poll",
            browserLoginUrl = "https://cloud.example.com/login/v2/flow/abc",
        )

    @Test
    fun `awaitCompletion polls through not-done-yet responses to a success and persists credentials`() =
        runTest {
            val api =
                FakeLoginFlowV2Api(
                    mutableListOf(
                        FakeLoginFlowV2Api.notDoneYet(),
                        FakeLoginFlowV2Api.notDoneYet(),
                        FakeLoginFlowV2Api.success("https://cloud.example.com", "alice", "app-password-123"),
                    ),
                )

            val result = repository.awaitCompletion(session(api), pollIntervalMillis = 1)

            check(result is LoginFlowResult.Success)
            assertEquals(Credentials("https://cloud.example.com", "alice", "app-password-123"), result.credentials)
            assertEquals(3, api.pollCallCount)
            assertEquals(Credentials("https://cloud.example.com", "alice", "app-password-123"), credentialStore.load())
        }

    @Test
    fun `awaitCompletion times out if the browser login never completes`() =
        runTest {
            val api = FakeLoginFlowV2Api(MutableList(LARGE_POLL_BUDGET) { FakeLoginFlowV2Api.notDoneYet() })

            val result = repository.awaitCompletion(session(api), pollIntervalMillis = 1, timeoutMillis = 20)

            assertTrue(result is LoginFlowResult.TimedOut)
            assertNull(credentialStore.load())
        }

    @Test
    fun `awaitCompletion turns a thrown exception into an Error result`() =
        runTest {
            val api = FakeLoginFlowV2Api().apply { pollError = java.io.IOException("connection reset") }

            val result = repository.awaitCompletion(session(api), pollIntervalMillis = 1)

            check(result is LoginFlowResult.Error)
            assertEquals("connection reset", result.message)
        }

    private companion object {
        const val LARGE_POLL_BUDGET = 1_000
    }
}
