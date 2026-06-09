package com.leon.be_nobat.domain.usecase

import com.leon.be_nobat.domain.model.User
import com.leon.be_nobat.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, pass: String): Result<User> {
        if (!email.contains("@")) {
            return Result.failure(Exception("ایمیل نامعتبر است"))
        }
        return repository.loginWithEmail(email, pass)
    }
}