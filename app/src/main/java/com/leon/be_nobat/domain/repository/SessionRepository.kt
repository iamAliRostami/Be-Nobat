package com.leon.be_nobat.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val token: Flow<String?>

    suspend fun saveToken(token: String)

    suspend fun clearToken()
}
