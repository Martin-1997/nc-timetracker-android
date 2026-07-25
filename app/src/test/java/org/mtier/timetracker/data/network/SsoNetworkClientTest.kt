package org.mtier.timetracker.data.network

import com.nextcloud.android.sso.QueryParam
import com.nextcloud.android.sso.api.AidlNetworkRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import okhttp3.Request as OkHttpRequest

/**
 * Covers the pure request/response mapping [SsoNetworkClient] delegates to
 * (buildNextcloudRequest/buildSsoResponse/buildSsoErrorResponse) — split out
 * specifically so this doesn't need a real Context/AIDL connection to the
 * Nextcloud Files app (see SsoNetworkClient's kdoc). currentApi() itself
 * (account resolution + the actual AIDL round-trip) isn't covered here for
 * that same reason; it was verified live against a real device instead.
 */
class SsoNetworkClientTest {
    @Test
    fun `buildNextcloudRequest carries over method, path, query parameters, and headers`() {
        val request =
            OkHttpRequest
                .Builder()
                .url("https://dynamic.invalid/apps/timetracker/ajax/work-intervals?from=1&to=2")
                .header("OCS-APIRequest", "true")
                .get()
                .build()

        val ncRequest = buildNextcloudRequest(request)

        assertEquals("GET", ncRequest.method)
        // The path alone — query parameters go through the structured
        // QueryParam mechanism below, not appended to the url string (see
        // this function's kdoc for why: the Files app's own OwnCloudClient
        // silently drops anything appended after "?" directly in url).
        assertEquals("/apps/timetracker/ajax/work-intervals", ncRequest.url)
        assertEquals(
            listOf(QueryParam("from", "1"), QueryParam("to", "2")),
            ncRequest.parameterV2.toList(),
        )
        assertEquals(listOf("true"), ncRequest.header["OCS-APIRequest"])
        assertNull(ncRequest.requestBody)
    }

    @Test
    fun `buildNextcloudRequest carries the request body as text`() {
        val request =
            OkHttpRequest
                .Builder()
                .url("https://dynamic.invalid/apps/timetracker/ajax/projects")
                .post("name=sso-aidl-test".toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()

        val ncRequest = buildNextcloudRequest(request)

        assertEquals("POST", ncRequest.method)
        assertEquals("name=sso-aidl-test", ncRequest.requestBody)
    }

    @Test
    fun `buildNextcloudRequest has no query parameters when there aren't any`() {
        val request = OkHttpRequest.Builder().url("https://dynamic.invalid/apps/timetracker/ajax/tags").get().build()

        val ncRequest = buildNextcloudRequest(request)

        assertEquals("/apps/timetracker/ajax/tags", ncRequest.url)
        assertEquals(emptyList<QueryParam>(), ncRequest.parameterV2.toList())
    }

    @Test
    fun `buildSsoResponse maps a successful AIDL result to an HTTP 200`() {
        val request = OkHttpRequest.Builder().url("https://dynamic.invalid/apps/timetracker/ajax/tags").get().build()
        val body = """{"tags":[]}""".toByteArray()
        val result =
            com.nextcloud.android.sso.api.Response(
                ByteArrayInputStream(body),
                arrayListOf(AidlNetworkRequest.PlainHeader("Content-Type", "application/json")),
            )

        val response = buildSsoResponse(request, result)

        assertEquals(200, response.code)
        assertEquals("application/json", response.header("Content-Type"))
        assertEquals("""{"tags":[]}""", response.body?.string())
    }

    @Test
    fun `buildSsoErrorResponse carries the given status code and message into the Response`() {
        val request = OkHttpRequest.Builder().url("https://dynamic.invalid/apps/timetracker/ajax/tags").get().build()

        val response = buildSsoErrorResponse(request, 429, "Reached maximum delay")

        assertEquals(429, response.code)
        assertEquals("Reached maximum delay", response.message)
        assertEquals("Reached maximum delay", response.body?.string())
    }

    @Test
    fun `buildSsoErrorResponse falls back to a generic message when none is given`() {
        val request = OkHttpRequest.Builder().url("https://dynamic.invalid/apps/timetracker/ajax/tags").get().build()

        val response = buildSsoErrorResponse(request, 502, null)

        assertEquals(502, response.code)
        assertEquals("HTTP 502", response.message)
    }
}
