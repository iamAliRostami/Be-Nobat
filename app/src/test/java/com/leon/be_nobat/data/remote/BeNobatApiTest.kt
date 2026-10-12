package com.leon.be_nobat.data.remote

import com.leon.be_nobat.data.ApiFailure
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class BeNobatApiTest {
    @Test fun loginUsesDotNetContractWithoutAnOldBearerToken() = runBlocking<Unit> {
        val engine = MockEngine { request ->
            assertEquals("/api/v1/auth/login", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)
            assertNull(request.headers[HttpHeaders.Authorization])
            respond("""{"accessToken":"new","user":{"id":"u","displayName":"Customer"}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        HttpClient(engine).use { client ->
            val api = BeNobatApi(client, { "https://example.test" }, { "old-pocketbase-token" })
            val result = api.request("auth/login", "POST",
                JSONObject().put("email", "customer@example.test").put("password", "secret1"), emptyMap())
            assertEquals("new", result.getString("accessToken"))
        }
    }

    @Test fun protectedCallsReadCurrentTokenAndEncodeQueryValues() = runBlocking<Unit> {
        var token = "first"
        val engine = MockEngine { request ->
            assertEquals("Bearer ${token}", request.headers[HttpHeaders.Authorization])
            assertEquals("تهران & north", request.url.parameters["city"])
            respond("""{"items":[]}""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        HttpClient(engine).use { client ->
            val api = BeNobatApi(client, { "https://example.test/" }, { token })
            api.request("appointments", "GET", null, mapOf("city" to "تهران & north"))
            token = "replacement"
            api.request("appointments", "GET", null, mapOf("city" to "تهران & north"))
        }
    }

    @Test fun expiredSessionKeepsStatusAndStableErrorCode() = runBlocking<Unit> {
        HttpClient(MockEngine {
            respond("""{"code":"unauthorized","message":"Expired"}""", HttpStatusCode.Unauthorized,
                headersOf(HttpHeaders.ContentType, "application/json"))
        }).use { client ->
            val error = runCatching {
                BeNobatApi(client, { "https://example.test" }, { "expired" })
                    .request("me", "GET", null, emptyMap())
            }.exceptionOrNull() as ApiFailure
            assertEquals(401, error.status)
            assertEquals("unauthorized", error.code)
        }
    }

    @Test fun noContentLogoutIsSuccessful() = runBlocking<Unit> {
        HttpClient(MockEngine { respond("", HttpStatusCode.NoContent) }).use { client ->
            assertEquals(0, BeNobatApi(client, { "https://example.test" }, { "token" })
                .request("auth/logout", "POST", null, emptyMap()).length())
        }
    }

    @Test fun avatarCannotSendRequestsToAnotherOrigin() = runBlocking<Unit> {
        HttpClient(MockEngine { error("Unexpected external request") }).use { client ->
            val failure = runCatching {
                BeNobatApi(client, { "https://example.test" }, { "token" }).avatar("https://other.test/avatar")
            }.exceptionOrNull()
            assertEquals("invalid_avatar", (failure as ApiFailure).code)
        }
    }
}
