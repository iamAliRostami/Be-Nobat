package com.leon.be_nobat.di.module

import android.app.Application
import com.leon.be_nobat.BuildConfig
import com.leon.be_nobat.data.SecureSession
import com.leon.be_nobat.data.text
import com.leon.be_nobat.data.remote.BeNobatApi
import com.leon.be_nobat.data.repository.BookingRepositoryImpl
import com.leon.be_nobat.domain.BookingRules
import com.leon.be_nobat.domain.repository.BookingRepository
import com.leon.be_nobat.domain.usecase.BookingUseCases
import com.leon.be_nobat.ui.NativeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val bookingModule = module {
    single { SecureSession(androidContext()) }
    single(named("bookingHttp")) {
        HttpClient(Android) {
            expectSuccess = false
            followRedirects = false
            install(HttpTimeout) {
                requestTimeoutMillis = 45_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }
        }
    }
    single {
        val preferences = androidContext().getSharedPreferences("settings", 0)
        val session = get<SecureSession>()
        BeNobatApi(get(named("bookingHttp")), {
            val url = preferences.getString("server_url", null) ?: BuildConfig.API_BASE_URL
            runCatching { BookingRules.serverUrl(url, BuildConfig.DEBUG) }.getOrDefault("")
        }, { session.read()?.text("accessToken") })
    }
    single<BookingRepository> { BookingRepositoryImpl(get()) }
    factory { BookingUseCases(get()) }
    viewModel { NativeViewModel(androidContext().applicationContext as Application, get(), get()) }
}
