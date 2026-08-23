package com.leon.be_nobat.helpers

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.leon.be_nobat.data.local.ThemeManager
import com.leon.be_nobat.di.module.localStorageModule
import com.leon.be_nobat.di.module.networkModule
import com.leon.be_nobat.di.module.repositoryModule
import com.leon.be_nobat.di.module.viewModelModule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

const val PREFERENCES_NAME = "setting_prefs"

class App : Application() {
    companion object {
        const val TAG = "BeNobat"
        const val API_TAG = "BeNobat.Api"
        const val DEFAULT_LANGUAGE_TAG = "fa"
    }

    override fun onCreate() {
        super.onCreate()
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(DEFAULT_LANGUAGE_TAG)
            )
        }
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                listOf(
                    localStorageModule,
                    repositoryModule,
                    viewModelModule,
                    networkModule
                )
            )
        }
        val preferences: ThemeManager = get()
        runBlocking {
            val savedTheme = preferences.themeMode.first()
            AppCompatDelegate.setDefaultNightMode(savedTheme)
        }
    }
}