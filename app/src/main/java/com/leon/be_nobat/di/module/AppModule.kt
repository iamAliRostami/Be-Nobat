package com.leon.be_nobat.di.module

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.leon.be_nobat.data.local.CryptoManager
import com.leon.be_nobat.data.local.ThemeManager
import com.leon.be_nobat.data.local.TokenManager
import com.leon.be_nobat.data.remote.PocketBaseClient
import com.leon.be_nobat.data.remote.PocketBaseConfig
import com.leon.be_nobat.data.repository.AuthRepositoryImpl
import com.leon.be_nobat.domain.interfaces.ICryptoManager
import com.leon.be_nobat.domain.repository.AuthRepository
import com.leon.be_nobat.domain.repository.SessionRepository
import com.leon.be_nobat.domain.usecase.LoginUseCase
import com.leon.be_nobat.helpers.PREFERENCES_NAME
import com.leon.be_nobat.ui.view_models.auth.AuthViewModel
import com.leon.be_nobat.ui.view_models.base.MainViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient(Android) {
            expectSuccess = false
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}
val localStorageModule = module {
    single {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile(PREFERENCES_NAME) }
        )
    }
    single<ICryptoManager> { CryptoManager() }
    single { TokenManager(get(), get()) }
    single<SessionRepository> { get<TokenManager>() }
    single { ThemeManager(get()) }
}
val repositoryModule = module {
    // جفت کردن اینترفیس و پیاده‌سازی ریپازیتوری
    single<AuthRepository> {
        AuthRepositoryImpl(get(), get())
    }
    // تعریف UseCaseها
    factory { LoginUseCase(get()) }
}

val viewModelModule = module {
    // تعریف ViewModelها برای لایه Presentation
    viewModel { AuthViewModel(get()) }
    viewModel { MainViewModel(get()) }
}
val networkModule = module {
    single {
        val sessionRepository: SessionRepository = get()
        PocketBaseClient(
            client = get(),
            baseUrl = PocketBaseConfig.BASE_URL,
            tokenProvider = { sessionRepository.token.first() }
        )
    }
}
