package com.leon.be_nobat.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.leon.be_nobat.domain.interfaces.ICryptoManager
import com.leon.be_nobat.domain.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import okio.Path.Companion.toPath
import okio.FileSystem
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferencesSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class TokenManagerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `save persists token and complete user and delete clears both`() = runBlocking {
        val storageScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(
            scope = storageScope,
            // Okio uses atomic replacement on Windows; Android FileStorage uses File.renameTo.
            storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) {
                File(temporaryFolder.root, "session.preferences_pb").absolutePath.toPath()
            },
        )
        try {
        val manager = TokenManager(dataStore, Json, FakeCryptoManager)
        val user = User(
            email = "user@example.com",
            emailVisibility = true,
            verified = true,
            name = "Test User",
            avatar = "avatar.png",
            mobile = "+989121234567",
            status = "active",
        )

        manager.save("access-token", user)

        assertEquals("access-token", manager.userToken.first())
        assertEquals(user, manager.user.first())

        manager.deleteToken()

        assertNull(manager.userToken.first())
        assertNull(manager.user.first())
        } finally { storageScope.cancel() }
    }

    private object FakeCryptoManager : ICryptoManager {
        override fun encrypt(threshold: String): String = "encrypted:$threshold"

        override fun decrypt(encryptedData: String): String =
            encryptedData.removePrefix("encrypted:")
    }
}
