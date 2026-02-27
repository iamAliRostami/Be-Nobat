package com.leon.be_nobat.di.module

import com.leon.be_nobat.helpers.TokenManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { TokenManager(androidContext()) }
}