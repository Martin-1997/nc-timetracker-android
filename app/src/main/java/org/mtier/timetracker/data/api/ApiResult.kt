package org.mtier.timetracker.data.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.Response

class ApiException(message: String) : Exception(message)

/**
 * Every mutation endpoint returns HTTP 200 with an embedded "Error" (or,
 * inconsistently, lowercase "error") key on failure, except add-cost, which
 * uses HTTP 400 for an invalid value. This uniformly surfaces either as a
 * thrown [ApiException] so repositories only need one catch block.
 */
fun Response<JsonObject>.throwOnError(): JsonObject? {
    val body = body()
    val bodyError = body?.errorMessage()
    if (bodyError != null) throw ApiException(bodyError)

    if (!isSuccessful) {
        val fromErrorBody = errorBody()?.string()?.let { raw ->
            runCatching { Json.parseToJsonElement(raw) as? JsonObject }.getOrNull()?.errorMessage()
        }
        throw ApiException(fromErrorBody ?: "Request failed (HTTP ${code()})")
    }

    return body
}

private fun JsonObject.errorMessage(): String? {
    val value = this["Error"] ?: this["error"] ?: return null
    val primitive = value as? JsonPrimitive ?: return "Request failed"
    return if (primitive.isString) primitive.content else "Request failed"
}
