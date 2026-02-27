package com.leon.be_nobat.helpers

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.leon.be_nobat.helpers.interfaces.ITokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "secure_token_prefs")

class TokenManager(context: Context) : ITokenManager {
    private val dataStore = context.dataStore
    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]?.let {
            try {
                CryptoManager.decrypt(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("encrypted_token")
    }

    override suspend fun saveToken(token: String) {
        val encryptedToken = CryptoManager.encrypt(token)
        dataStore.edit { it[TOKEN_KEY] = encryptedToken }
    }

    override suspend fun getToken(): String? {
        val prefs = dataStore.data.first()
        return prefs[TOKEN_KEY]?.let {
            try {
                CryptoManager.decrypt(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun deleteToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
}