package com.leon.be_nobat.di.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.leon.be_nobat.data.token.TokenManager
import com.leon.be_nobat.domain.interfaces.ITokenManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val tokenModule = module {
    single { provideDataStore(androidContext()) }
    single<ITokenManager> { TokenManager(get()) }
}

fun provideDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("secure_token_prefs") }
    )
}