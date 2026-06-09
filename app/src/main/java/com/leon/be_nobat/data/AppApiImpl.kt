package com.leon.be_nobat.data

import com.leon.be_nobat.domain.model.User
import com.leon.be_nobat.domain.interfaces.IAppApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AppApiImpl(
    private val client: HttpClient,
    private val dispatcher: CoroutineDispatcher   // from DI
) : IAppApi {

    override suspend fun fetchUsers(): List<User> = withContext(dispatcher) {
        client.get("https://api.example.com/users").body()   // Ktor auto‑serializes to `List<User>`
    }

    // Add more methods as needed …
}