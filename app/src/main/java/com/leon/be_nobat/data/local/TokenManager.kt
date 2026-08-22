package com.leon.be_nobat.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import com.leon.be_nobat.domain.interfaces.ICryptoManager
import com.leon.be_nobat.domain.repository.SessionRepository


class TokenManager(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: ICryptoManager,
) : SessionRepository {
    companion object {
        private const val NAME = "encrypted_token"
        private val KEY_USER_TOKEN = stringPreferencesKey(NAME)
    }

    override val token: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_USER_TOKEN]?.let {
            runCatching {
                cryptoManager.decrypt(it)
            }.getOrNull()
        }
    }

    override suspend fun saveToken(token: String) {
        val encryptedToken = cryptoManager.encrypt(token)
        dataStore.edit { preferences ->
            preferences[KEY_USER_TOKEN] = encryptedToken
        }
    }

    override suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_USER_TOKEN)
        }
    }
}
