package org.mtier.timetracker.data.api

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Response

/** Every mutation endpoint reports failure inconsistently — HTTP 200 with an
 *  embedded "Error"/"error" key, or a real non-2xx status with a JSON error
 *  body (add-cost) — throwOnError() is the single place that normalizes both. */
class ApiResultTest {
    @Test
    fun `2xx with no error key returns the body`() {
        val body = JsonObject(mapOf("id" to JsonPrimitive(1)))
        val result = Response.success(body).throwOnError()
        assertEquals(body, result)
    }

    @Test
    fun `2xx with capitalized Error key throws`() {
        val body = JsonObject(mapOf("Error" to JsonPrimitive("name already exists")))
        val exception = assertThrows(ApiException::class.java) { Response.success(body).throwOnError() }
        assertEquals("name already exists", exception.message)
    }

    @Test
    fun `2xx with lowercase error key throws`() {
        val body = JsonObject(mapOf("error" to JsonPrimitive("nope")))
        val exception = assertThrows(ApiException::class.java) { Response.success(body).throwOnError() }
        assertEquals("nope", exception.message)
    }

    @Test
    fun `non-2xx with json error body throws the parsed message`() {
        val errorJson = """{"Error":"invalid cost value"}"""
        val response =
            Response.error<JsonObject>(
                HTTP_BAD_REQUEST,
                errorJson.toResponseBody("application/json".toMediaType()),
            )
        val exception = assertThrows(ApiException::class.java) { response.throwOnError() }
        assertEquals("invalid cost value", exception.message)
    }

    @Test
    fun `non-2xx with unparsable body falls back to a generic message with the status code`() {
        val response =
            Response.error<JsonObject>(
                HTTP_SERVER_ERROR,
                "<html>gateway timeout</html>".toResponseBody("text/html".toMediaType()),
            )
        val exception = assertThrows(ApiException::class.java) { response.throwOnError() }
        assertEquals("Request failed (HTTP $HTTP_SERVER_ERROR)", exception.message)
    }

    @Test
    fun `2xx with a null body returns null`() {
        val response = Response.success<JsonObject>(null)
        assertNull(response.throwOnError())
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
        const val HTTP_SERVER_ERROR = 502
    }
}
