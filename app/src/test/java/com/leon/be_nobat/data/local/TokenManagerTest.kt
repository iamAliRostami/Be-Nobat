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

class TokenManagerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `save persists token and complete user and delete clears both`() = runBlocking {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(temporaryFolder.root, "session.preferences_pb") },
        )
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
    }

    private object FakeCryptoManager : ICryptoManager {
        override fun encrypt(threshold: String): String = "encrypted:$threshold"

        override fun decrypt(encryptedData: String): String =
            encryptedData.removePrefix("encrypted:")
    }
}
