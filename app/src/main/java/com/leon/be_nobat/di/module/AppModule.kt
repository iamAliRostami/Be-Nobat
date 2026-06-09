package com.leon.be_nobat.di.module

import com.leon.be_nobat.data.AppApiImpl
import com.leon.be_nobat.data.remote.NetworkManger
import com.leon.be_nobat.data.local.TokenManager
import com.leon.be_nobat.data.repository.AuthRepositoryImpl
import com.leon.be_nobat.domain.interfaces.IAppApi
import com.leon.be_nobat.domain.repository.AuthRepository
import com.leon.be_nobat.domain.usecase.LoginUseCase
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.leon.be_nobat.ui.view_models.auth.AuthViewModel
import org.koin.core.module.dsl.viewModel

val appModule = module {
    single { TokenManager(androidContext()) }
    // ---- Coroutine dispatcher --------------------------------------------
    single<CoroutineDispatcher> { Dispatchers.IO }
    // ---- HttpClient ------------------------------------------------------
    single { NetworkManger.provideHttpClient() }
    // ---- ApiService implementation --------------------------------------
    single<IAppApi> { AppApiImpl(get<HttpClient>(), get<CoroutineDispatcher>()) }
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