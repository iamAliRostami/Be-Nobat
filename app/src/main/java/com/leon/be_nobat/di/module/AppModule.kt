package com.leon.be_nobat.di.module

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.leon.be_nobat.data.local.ThemeManager
import com.leon.be_nobat.data.local.TokenManager
import com.leon.be_nobat.data.remote.PocketBaseClient
import com.leon.be_nobat.data.remote.PocketBaseConfig
import com.leon.be_nobat.data.repository.AuthRepositoryImpl
import com.leon.be_nobat.domain.repository.AuthRepository
import com.leon.be_nobat.domain.usecase.LoginUseCase
import com.leon.be_nobat.helpers.PREFERENCES_NAME
import com.leon.be_nobat.ui.view_models.auth.AuthViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val localStorageModule = module {
    single {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile(PREFERENCES_NAME) }
        )
    }
    single { TokenManager(get()) }
    single { ThemeManager(get()) }
}
val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factory { LoginUseCase(get()) }
}

val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
}
val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        }
    }
    single {
        HttpClient(Android) {
            expectSuccess = false
            install(ContentNegotiation) { json(get()) }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
        }
    }
    single {
        val tokenManager = get<TokenManager>()
        PocketBaseClient(
            client = get(),
            baseUrl = PocketBaseConfig.baseUrl,
            loginUrl = PocketBaseConfig.loginUrl,
            tokenProvider = { tokenManager.userToken.first() },
        )
    }
}
