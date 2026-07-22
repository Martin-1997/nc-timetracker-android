package org.mtier.timetracker.data.network

import android.content.Context
import com.google.gson.Gson
import com.nextcloud.android.sso.aidl.NextcloudRequest
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.exceptions.NextcloudHttpRequestFailedException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes requests through the Nextcloud Files app's AIDL relay instead of
 * direct HTTP. [org.mtier.timetracker.data.auth.Credentials.appPassword]
 * isn't a usable server credential when
 * [org.mtier.timetracker.data.auth.Credentials.isSso] is true — see that
 * class's kdoc — so [AuthInterceptor] delegates to this instead of Basic
 * Auth for SSO-authenticated sessions.
 *
 * NextcloudAPI is kept open across calls per the library's own guidance
 * (rebinding its backing service on every request is expensive) and rebuilt
 * only if the active account changes.
 */
@Singleton
class SsoNetworkClient
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val gson = Gson()

        @Volatile
        private var cached: Pair<String, NextcloudAPI>? = null

        private fun currentApi(): NextcloudAPI {
            val account = SingleAccountHelper.getCurrentSingleSignOnAccount(context)
            cached?.let { (accountName, api) -> if (accountName == account.name) return api }
            cached?.second?.close()
            val api = NextcloudAPI(context, account, gson)
            cached = account.name to api
            return api
        }

        fun execute(request: Request): Response {
            val api = currentApi()
            val query = request.url.encodedQuery?.let { "?$it" } ?: ""
            val ncRequestBuilder =
                NextcloudRequest
                    .Builder()
                    .setMethod(request.method)
                    .setUrl(request.url.encodedPath + query)
                    .setHeader(request.headers.toMultimap())
            request.bodyAsUtf8String()?.let(ncRequestBuilder::setRequestBody)
            val ncRequest = ncRequestBuilder.build()

            return try {
                val result = api.performNetworkRequestV2(ncRequest)
                val headers = result.plainHeaders.associate { it.name to it.value }.toHeaders()
                val contentType = result.getPlainHeader("Content-Type")?.value?.toMediaTypeOrNull()
                Response
                    .Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(HTTP_OK)
                    .message("OK")
                    .headers(headers)
                    .body(result.body.readBytes().toResponseBody(contentType))
                    .build()
            } catch (e: NextcloudHttpRequestFailedException) {
                errorResponse(request, e.statusCode, e.message)
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                // Anything else the AIDL relay itself can throw (the Files
                // app service not responding, a malformed request, etc.) —
                // not a real HTTP status, so there's nothing more specific
                // to map it to. Callers already treat any non-2xx as a
                // generic failure (see ApiResult.throwOnError).
                errorResponse(request, HTTP_BAD_GATEWAY, e.message)
            }
        }

        private fun errorResponse(
            request: Request,
            code: Int,
            message: String?,
        ): Response =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(message ?: "HTTP $code")
                .body((message ?: "").toResponseBody(null))
                .build()

        private fun Request.bodyAsUtf8String(): String? {
            val requestBody = body ?: return null
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            return buffer.readUtf8()
        }

        private companion object {
            const val HTTP_OK = 200
            const val HTTP_BAD_GATEWAY = 502
        }
    }
