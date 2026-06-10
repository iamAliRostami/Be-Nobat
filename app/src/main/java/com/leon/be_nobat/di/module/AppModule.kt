package com.leon.be_nobat.di.module

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.leon.be_nobat.data.AppApiImpl
import com.leon.be_nobat.data.local.ThemeManager
import com.leon.be_nobat.data.remote.NetworkManger
import com.leon.be_nobat.data.local.TokenManager
import com.leon.be_nobat.data.repository.AuthRepositoryImpl
import com.leon.be_nobat.domain.interfaces.IAppApi
import com.leon.be_nobat.domain.repository.AuthRepository
import com.leon.be_nobat.domain.usecase.LoginUseCase
import com.leon.be_nobat.helpers.PREFERENCES_NAME
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.leon.be_nobat.ui.view_models.auth.AuthViewModel
import org.koin.core.module.dsl.viewModel

val appModule = module {
    single<CoroutineDispatcher> { Dispatchers.IO }
    single { NetworkManger.provideHttpClient() }
    single<IAppApi> { AppApiImpl(get<HttpClient>(), get<CoroutineDispatcher>()) }
}
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
}