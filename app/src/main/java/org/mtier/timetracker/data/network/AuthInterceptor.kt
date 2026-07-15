package org.mtier.timetracker.data.network

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import org.mtier.timetracker.data.auth.CredentialStore
import javax.inject.Inject
import okhttp3.Credentials as OkHttpBasicCredentials

/**
 * Retrofit is built once with a placeholder base URL (the account's real
 * server isn't known until after Login Flow v2 completes, and can change if
 * the user signs out and into a different server). This interceptor
 * rewrites every outgoing request to the currently stored server and
 * attaches HTTP Basic Auth with the stored app password — confirmed
 * against a live Nextcloud instance to work without any CSRF token on
 * every AjaxController endpoint, including ones without @NoCSRFRequired
 * (see PLAN.md §2).
 */
class AuthInterceptor
    @Inject
    constructor(
        private val credentialStore: CredentialStore,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val credentials = credentialStore.load() ?: return chain.proceed(original)
            val serverUrl = credentials.serverUrl.toHttpUrlOrNull() ?: return chain.proceed(original)

            val rewrittenUrl =
                original.url
                    .newBuilder()
                    .scheme(serverUrl.scheme)
                    .host(serverUrl.host)
                    .port(serverUrl.port)
                    .build()

            val authenticated =
                original
                    .newBuilder()
                    .url(rewrittenUrl)
                    .header(
                        "Authorization",
                        OkHttpBasicCredentials.basic(credentials.username, credentials.appPassword),
                    ).header("OCS-APIRequest", "true")
                    .build()

            return chain.proceed(authenticated)
        }
    }
