package com.leon.be_nobat.domain.usecase

import com.leon.be_nobat.domain.model.User
import com.leon.be_nobat.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {
    private val repository = RecordingAuthRepository()
    private val useCase = LoginUseCase(repository)

    @Test
    fun `invalid email is rejected without calling repository`() = runBlocking {
        val result = useCase("not-an-email", "secret")

        assertTrue(result.isFailure)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `blank password is rejected without calling repository`() = runBlocking {
        val result = useCase("user@example.com", "  ")

        assertTrue(result.isFailure)
        assertEquals(0, repository.calls)
    }

    @Test
    fun `valid credentials are normalized and delegated`() = runBlocking {
        val result = useCase("  user@example.com  ", "secret")

        assertFalse(result.isSuccess)
        assertEquals(1, repository.calls)
        assertEquals("user@example.com", repository.email)
    }

    private class RecordingAuthRepository : AuthRepository {
        var calls = 0
        var email: String? = null

        override suspend fun loginWithEmail(email: String, password: String): Result<User> {
            calls++
            this.email = email
            return Result.failure(UnsupportedOperationException("Network is not part of this test"))
        }
    }
}
