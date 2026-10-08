package com.leon.be_nobat.data.network

import com.leon.be_nobat.domain.interfaces.IApiService
import com.leon.be_nobat.domain.interfaces.ITokenManager
import io.ktor.client.HttpClient

class ApiService @Inject constructor(
    private val client: HttpClient,
    private val tokenManager: ITokenManager,
): IApiService {
}