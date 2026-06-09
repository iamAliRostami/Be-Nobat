package com.leon.be_nobat.domain.interfaces

import io.ktor.client.HttpClient

interface INetworkManager {
    fun provideHttpClient(): HttpClient
    fun provideApiService(client: HttpClient): IAppApi
}