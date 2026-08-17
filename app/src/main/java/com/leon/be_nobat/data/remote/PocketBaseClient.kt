package com.leon.be_nobat.data.remote

import com.leon.be_nobat.data.remote.PocketBaseConfig.Companion.BASE_URL
import com.leon.be_nobat.domain.model.User
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PocketBaseException(val status: Int, message: String) : Exception(message)

@Serializable
data class PbListResponse<T>(
    val page: Int = 0,
    val perPage: Int = 0,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val items: List<T> = emptyList(),
)

@Serializable
data class PbAuthResponse(
    val token: String = "",
    val record: User,
)

class PocketBaseClient(
    private val tokenProvider: suspend () -> String? = { null },
) {
    @PublishedApi
    internal val client = HttpClient(Android) { expectSuccess = false }

    @PublishedApi
    internal val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    @PublishedApi
    internal suspend fun currentToken(): String? = tokenProvider()

    @PublishedApi
    internal fun HttpRequestBuilder.auth(token: String?) {
        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    @PublishedApi
    internal suspend fun HttpResponse.ensureSuccess(): HttpResponse {
        if (!status.isSuccess()) throw PocketBaseException(status.value, bodyAsText())
        return this
    }

    // ---------- لیست ----------
    suspend inline fun <reified T> list(
        collection: String,
        page: Int = 1,
        perPage: Int = 30,
        filter: String? = null,
        sort: String? = null,
        expand: String? = null,
    ): Result<PbListResponse<T>> = runCatching {
        val token = currentToken()
        client.get("$BASE_URL/api/collections/$collection/records") {
            parameter("page", page)
            parameter("perPage", perPage)
            filter?.let { parameter("filter", it) }
            sort?.let { parameter("sort", it) }
            expand?.let { parameter("expand", it) }
            auth(token)
        }.ensureSuccess().let { json.decodeFromString<PbListResponse<T>>(it.bodyAsText()) }
    }

    // ---------- یک رکورد ----------
    suspend inline fun <reified T> get(
        collection: String, id: String, expand: String? = null,
    ): Result<T> = runCatching {
        val token = currentToken()
        client.get("$BASE_URL/api/collections/$collection/records/$id") {
            expand?.let { parameter("expand", it) }
            auth(token)
        }.ensureSuccess().let { json.decodeFromString<T>(it.bodyAsText()) }
    }

    // ---------- ساخت ----------
    suspend inline fun <reified T> create(
        collection: String, body: JsonObject,
    ): Result<T> = runCatching {
        val token = currentToken()
        client.post("$BASE_URL/api/collections/$collection/records") {
            contentType(ContentType.Application.Json)
            setBody(body.toString())
            auth(token)
        }.ensureSuccess().let { json.decodeFromString<T>(it.bodyAsText()) }
    }

    // ---------- آپدیت ----------
    suspend inline fun <reified T> update(
        collection: String, id: String, body: JsonObject,
    ): Result<T> = runCatching {
        val token = currentToken()
        client.patch("$BASE_URL/api/collections/$collection/records/$id") {
            contentType(ContentType.Application.Json)
            setBody(body.toString())
            auth(token)
        }.ensureSuccess().let { json.decodeFromString<T>(it.bodyAsText()) }
    }

    // ---------- حذف ----------
    suspend fun delete(collection: String, id: String): Result<Unit> = runCatching {
        val token = currentToken()
        client.delete("$BASE_URL/api/collections/$collection/records/$id") { auth(token) }
            .ensureSuccess()
        Unit
    }

    // ---------- ورود ----------
    suspend fun authWithPassword(identity: String, password: String): Result<PbAuthResponse> =
        runCatching {
            client.post("$BASE_URL/api/collections/users/auth-with-password") {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("identity", identity)
                    put("password", password)
                }.toString())
            }.ensureSuccess().let { json.decodeFromString<PbAuthResponse>(it.bodyAsText()) }
        }
}