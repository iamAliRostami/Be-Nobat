package com.leon.be_nobat.data.remote

import com.leon.be_nobat.data.ApiFailure
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URI

/** Ktor transport; callers use the domain repository contract. */
class BeNobatApi(
    private val client: HttpClient,
    private val baseUrl: () -> String,
    private val token: () -> String?
) {
    suspend fun request(
        path: String,
        method: String,
        body: JSONObject?,
        query: Map<String, String>
    ): JSONObject {
        if (baseUrl().isBlank()) throw ApiFailure("configuration")
        val response = client.request(baseUrl().trimEnd('/') + "/api/v1/" + path.trimStart('/')) {
            this.method = HttpMethod.parse(method)
            accept(ContentType.Application.Json)
            query.filterValues { it.isNotBlank() }.forEach { (key, value) -> parameter(key, value) }
            if (path !in listOf("auth/login", "auth/register")) token()?.takeIf(String::isNotBlank)
                ?.let { bearerAuth(it) }
            if (method !in listOf("GET", "HEAD")) {
                contentType(ContentType.Application.Json)
                setBody((body ?: JSONObject()).toString())
            }
        }
        val raw = response.bodyAsText()
        val parsed = runCatching {
            when {
                raw.isBlank() -> JSONObject()
                raw.trimStart().startsWith("[") -> JSONObject().put("items", JSONArray(raw))
                else -> JSONObject(raw)
            }
        }.getOrNull()
        if (!response.status.isSuccess()) throw ApiFailure(
            parsed?.optString("code")?.takeIf(String::isNotBlank) ?: "server_error",
            response.status.value
        )
        return parsed ?: throw ApiFailure("invalid_response")
    }

    suspend fun avatar(path: String): ByteArray {
        val base = URI(baseUrl().trimEnd('/') + "/")
        val uri = base.resolve(path)
        if (uri.host != base.host || uri.scheme != base.scheme || uri.port != base.port || uri.userInfo != null)
            throw ApiFailure("invalid_avatar")
        return client.prepareGet(uri.toString()).execute { response ->
            if (!response.status.isSuccess()) throw ApiFailure("invalid_avatar")
            val input = response.bodyAsChannel()
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.readAvailable(buffer, 0, buffer.size)
                if (count < 0) break
                if (output.size() + count > 2 * 1024 * 1024) throw ApiFailure("invalid_avatar")
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    }
}