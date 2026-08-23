package com.leon.be_nobat.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class TokenManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private const val NAME = "encrypted_token"
        private val KEY_USER_TOKEN = stringPreferencesKey(NAME)
    }

    val userToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_USER_TOKEN]?.let {
            runCatching {
                CryptoManager.decrypt(it)
            }.getOrNull()
        }
    }

    suspend fun save(token: String) {
        val encryptedToken = CryptoManager.encrypt(token)
        dataStore.edit { preferences ->
            preferences[KEY_USER_TOKEN] = encryptedToken
        }
    }

    suspend fun deleteToken() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_USER_TOKEN)
        }
    }
}
