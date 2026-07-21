package org.mtier.timetracker.data.auth

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

class LoginFlowSession(
    val serverUrl: String,
    internal val api: LoginFlowV2Api,
    internal val pollToken: String,
    val pollEndpoint: String,
    val browserLoginUrl: String,
)

sealed interface LoginFlowResult {
    data class Success(
        val credentials: Credentials,
    ) : LoginFlowResult

    data object TimedOut : LoginFlowResult

    data class Error(
        val message: String,
    ) : LoginFlowResult
}

@Singleton
class AuthRepository
    @Inject
    constructor(
        private val credentialStore: CredentialStorage,
    ) {
        fun currentCredentials(): Credentials? = credentialStore.load()

        fun signOut() = credentialStore.clear()

        /**
         * Normalizes a user-entered server address (adds https:// if missing,
         * strips a trailing slash) and starts a Login Flow v2 session against
         * it. Throws on network/parse errors — the caller (ViewModel) turns
         * that into a user-facing error message.
         */
        suspend fun beginLoginFlow(rawServerUrl: String): LoginFlowSession {
            val serverUrl = normalizeServerUrl(rawServerUrl)
            val api = buildLoginFlowApi(serverUrl)
            val response = api.initiate()
            return LoginFlowSession(
                serverUrl = serverUrl,
                api = api,
                pollToken = response.poll.token,
                pollEndpoint = response.poll.endpoint,
                browserLoginUrl = response.login,
            )
        }

        /**
         * Polls until the user completes the browser login, the given timeout
         * elapses, or a non-recoverable error occurs. The server returns 404
         * for "not done yet", which is the expected steady state while waiting.
         */
        @Suppress("TooGenericExceptionCaught")
        suspend fun awaitCompletion(
            session: LoginFlowSession,
            pollIntervalMillis: Long = DEFAULT_POLL_INTERVAL_MS,
            timeoutMillis: Long = DEFAULT_LOGIN_TIMEOUT_MS,
        ): LoginFlowResult =
            try {
                val credentials =
                    withTimeoutOrNull(timeoutMillis) {
                        var result: Credentials? = null
                        while (result == null) {
                            val response = session.api.poll(session.pollEndpoint, session.pollToken)
                            val body = response.body()
                            result =
                                if (response.isSuccessful && body != null) {
                                    Credentials(
                                        serverUrl = body.server.trimEnd('/'),
                                        username = body.loginName,
                                        appPassword = body.appPassword,
                                    )
                                } else {
                                    delay(pollIntervalMillis)
                                    null
                                }
                        }
                        result
                    }
                if (credentials != null) {
                    credentialStore.save(credentials)
                    LoginFlowResult.Success(credentials)
                } else {
                    LoginFlowResult.TimedOut
                }
            } catch (e: Exception) {
                LoginFlowResult.Error(e.message ?: "Unknown error")
            }

        private fun normalizeServerUrl(input: String): String {
            val trimmed = input.trim().trimEnd('/')
            return if (trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true)
            ) {
                trimmed
            } else {
                "https://$trimmed"
            }
        }

        private fun buildLoginFlowApi(serverUrl: String): LoginFlowV2Api {
            val json = Json { ignoreUnknownKeys = true }
            val okHttpClient = OkHttpClient.Builder().build()
            val retrofit =
                Retrofit
                    .Builder()
                    .baseUrl("$serverUrl/")
                    .client(okHttpClient)
                    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                    .build()
            return retrofit.create(LoginFlowV2Api::class.java)
        }

        private companion object {
            const val DEFAULT_POLL_INTERVAL_MS = 1_500L
            const val DEFAULT_LOGIN_TIMEOUT_MS = 10 * 60 * 1_000L
        }
    }
