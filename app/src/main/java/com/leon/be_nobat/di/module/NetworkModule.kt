package com.leon.be_nobat.di.module

import com.leon.be_nobat.data.network.ApiService
import com.leon.be_nobat.domain.interfaces.IApiService
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val networkModule = module {
    // ---- Coroutine dispatcher --------------------------------------------
    single<CoroutineDispatcher> { Dispatchers.IO }
    single {
        HttpClient(Android) {
            install(ContentNegotiation) { json() }
            install(Auth)
        }
    }

    // Factory for the API service interface
    factory<IApiService> {
        ApiService(
            client = get(),
            tokenManager = get()
        )
    }
}