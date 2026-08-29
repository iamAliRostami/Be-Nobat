package com.leon.be_nobat.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.leon.be_nobat.domain.interfaces.ICryptoManager
import com.leon.be_nobat.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class TokenManager(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
    private val cryptoManager: ICryptoManager,
) {
    companion object {
        private val KEY_USER_TOKEN = stringPreferencesKey("encrypted_token")
        private val KEY_USER = stringPreferencesKey("encrypted_user")
    }

    val userToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs.decrypt(KEY_USER_TOKEN)
    }

    val user: Flow<User?> = dataStore.data.map { prefs ->
        prefs.decrypt(KEY_USER)?.let { serializedUser ->
            runCatching { json.decodeFromString<User>(serializedUser) }.getOrNull()
        }
    }

    suspend fun save(token: String) {
        val encryptedToken = cryptoManager.encrypt(token)
        dataStore.edit { preferences ->
            preferences[KEY_USER_TOKEN] = encryptedToken
        }
    }

    /** Persists the complete successful login response in one DataStore transaction. */
    suspend fun save(token: String, user: User) {
        val encryptedToken = cryptoManager.encrypt(token)
        val encryptedUser = cryptoManager.encrypt(json.encodeToString(user))
        dataStore.edit { preferences ->
            preferences[KEY_USER_TOKEN] = encryptedToken
            preferences[KEY_USER] = encryptedUser
        }
    }

    suspend fun deleteToken() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_USER_TOKEN)
            preferences.remove(KEY_USER)
        }
    }

    private fun Preferences.decrypt(key: Preferences.Key<String>): String? =
        get(key)?.let { encryptedValue ->
            runCatching { cryptoManager.decrypt(encryptedValue) }.getOrNull()
        }
}
