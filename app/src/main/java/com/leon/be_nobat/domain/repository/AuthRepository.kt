package com.leon.be_nobat.domain.repository

import com.leon.be_nobat.domain.model.User

interface AuthRepository {
    suspend fun loginWithEmail(email: String, password: String): Result<User>
}
