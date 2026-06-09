package com.leon.be_nobat.domain.interfaces

interface ITokenManager {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun deleteToken()
}