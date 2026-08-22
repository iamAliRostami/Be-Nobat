package com.leon.be_nobat.domain.usecase

import com.leon.be_nobat.domain.model.User
import com.leon.be_nobat.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim()
        if (!EMAIL_PATTERN.matches(normalizedEmail)) {
            return Result.failure(IllegalArgumentException("ایمیل نامعتبر است"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("گذرواژه نمی‌تواند خالی باشد"))
        }
        return repository.loginWithEmail(normalizedEmail, password)
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
