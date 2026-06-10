package com.leon.be_nobat.data.local

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeManager(private val dataStore: DataStore<Preferences>)  {
    companion object {
        private const val NAME = "theme_mode"
        private val KEY_THEME_MODE = intPreferencesKey(NAME)
    }

    val themeMode: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    suspend fun save(mode: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun clearTheme() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_THEME_MODE)
        }
    }
}