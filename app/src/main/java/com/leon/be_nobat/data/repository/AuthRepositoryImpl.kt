package com.leon.be_nobat.data.repository

import com.leon.be_nobat.data.local.TokenManager
import com.leon.be_nobat.domain.model.User
import com.leon.be_nobat.domain.repository.AuthRepository
import io.ktor.client.HttpClient

class AuthRepositoryImpl(
    private val client: HttpClient,
    private val prefs: TokenManager
) : AuthRepository {

    override suspend fun loginWithEmail(email: String, pass: String): Result<User> {
        return try {
            // در اینجا می‌توانید از PocketBase یا Ktor Client استفاده کنید.
            // به عنوان نمونه یک کاربر فرضی بازگردانده می‌شود:
            val user = User(id = "1", name = "توسعه‌دهنده", email = email)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}