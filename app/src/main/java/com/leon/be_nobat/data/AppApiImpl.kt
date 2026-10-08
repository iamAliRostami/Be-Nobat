package com.leon.be_nobat.data

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AppApiImpl(
    private val client: HttpClient,
    @IoDispatcher private val dispatcher: CoroutineDispatcher   // from DI
) : IAppApi {

    override suspend fun fetchUsers(): List<User> = withContext(dispatcher) {
        client.get("https://api.example.com/users")   // Ktor auto‑serializes to `List<User>`
    }

    // Add more methods as needed …
}