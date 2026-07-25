package org.mtier.timetracker.data.network

import android.content.Context
import com.google.gson.Gson
import com.nextcloud.android.sso.QueryParam
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

        fun execute(request: Request): Response =
            try {
                buildSsoResponse(request, currentApi().performNetworkRequestV2(buildNextcloudRequest(request)))
            } catch (e: NextcloudHttpRequestFailedException) {
                buildSsoErrorResponse(request, e.statusCode, e.message)
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                // Anything else the AIDL relay itself can throw (the Files
                // app service not responding, a malformed request, etc.) —
                // not a real HTTP status, so there's nothing more specific
                // to map it to. Callers already treat any non-2xx as a
                // generic failure (see ApiResult.throwOnError).
                buildSsoErrorResponse(request, HTTP_BAD_GATEWAY, e.message)
            }
    }

private const val HTTP_OK = 200
private const val HTTP_BAD_GATEWAY = 502

/** Pure OkHttp Request → NextcloudRequest conversion, split out from
 *  [SsoNetworkClient.execute] so it's directly unit-testable without a real
 *  Context/AIDL connection (see that class's kdoc for why those can't run
 *  in a plain JVM test).
 *
 * Query parameters are passed via [NextcloudRequest]'s structured
 * `parameter`/[QueryParam] mechanism, not appended to the `url` string —
 * confirmed live against a real device that the Files app's own
 * OwnCloudClient only reads query parameters from that structured field
 * and silently drops anything appended after "?" directly in `url` (the
 * library's own Retrofit integration, NextcloudRetrofitServiceMethod,
 * does the same: it builds `url` from just the path and passes @Query
 * parameters separately). Baking the query into `url` instead — this
 * function's original implementation — reached the Files app fine but
 * silently never made it into the actual HTTP request sent to the
 * server, breaking every endpoint that depends on query parameters
 * (Reports/Dashboard's report endpoint hardest, since the server's own
 * SQL query builder produces invalid SQL when every filter is missing —
 * see PR description).
 */
internal fun buildNextcloudRequest(request: Request): NextcloudRequest {
    val queryParams =
        (0 until request.url.querySize).map { i ->
            QueryParam(request.url.queryParameterName(i), request.url.queryParameterValue(i) ?: "")
        }
    val builder =
        NextcloudRequest
            .Builder()
            .setMethod(request.method)
            .setUrl(request.url.encodedPath)
            .setParameter(queryParams)
            .setHeader(request.headers.toMultimap())
    request.bodyAsUtf8String()?.let(builder::setRequestBody)
    return builder.build()
}

/** Pure success-path mapping from the AIDL relay's result back to a real
 *  OkHttp Response — see [buildNextcloudRequest]'s kdoc for why this is
 *  split out. Always HTTP 200: the AIDL relay's Response type doesn't carry
 *  a status code on success (only body + headers), matching how the
 *  library's own Retrofit2Helper treats any non-exception result. */
internal fun buildSsoResponse(
    request: Request,
    result: com.nextcloud.android.sso.api.Response,
): Response {
    val headers = result.plainHeaders.associate { it.name to it.value }.toHeaders()
    val contentType = result.getPlainHeader("Content-Type")?.value?.toMediaTypeOrNull()
    return Response
        .Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(HTTP_OK)
        .message("OK")
        .headers(headers)
        .body(result.body.readBytes().toResponseBody(contentType))
        .build()
}

/** Pure failure-path mapping — see [buildNextcloudRequest]'s kdoc for why
 *  this is split out. */
internal fun buildSsoErrorResponse(
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
