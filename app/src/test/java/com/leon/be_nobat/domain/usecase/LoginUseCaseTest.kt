package com.leon.be_nobat.domain.usecase

import com.leon.be_nobat.domain.model.AuthException
import com.leon.be_nobat.domain.model.User
import com.leon.be_nobat.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {
    private val repository = RecordingAuthRepository()
    private val useCase = LoginUseCase(repository)

    @Test fun `valid email is normalized and passed to repository`() = runBlocking {
        val result = useCase("  User@Example.COM ", "secret")
        assertTrue(result.isSuccess)
        assertEquals("user@example.com", repository.lastIdentity)
    }

    @Test fun `iranian mobile is normalized to international format`() = runBlocking {
        val result = useCase("09121234567", "secret")
        assertTrue(result.isSuccess)
        assertEquals("+989121234567", repository.lastIdentity)
    }

    @Test fun `mobile with international prefix remains normalized`() = runBlocking {
        useCase("00989121234567", "secret")
        assertEquals("+989121234567", repository.lastIdentity)
    }

    @Test fun `invalid identity is rejected without calling repository`() = runBlocking {
        val result = useCase("invalid-identity", "secret")
        assertSame(AuthException.InvalidIdentifier, result.exceptionOrNull())
        assertEquals(0, repository.calls)
    }

    @Test fun `blank password is rejected without calling repository`() = runBlocking {
        val result = useCase("user@example.com", "  ")
        assertSame(AuthException.EmptyPassword, result.exceptionOrNull())
        assertEquals(0, repository.calls)
    }

    private class RecordingAuthRepository : AuthRepository {
        var calls = 0
        var lastIdentity: String? = null

        override suspend fun login(identity: String, password: String): Result<User> {
            calls++
            lastIdentity = identity
            return Result.success(User(name = "Test User"))
        }
    }
}
