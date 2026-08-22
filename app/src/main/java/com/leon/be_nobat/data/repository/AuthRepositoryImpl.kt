package com.leon.be_nobat.data.repository

import com.leon.be_nobat.data.remote.PocketBaseClient
import com.leon.be_nobat.domain.model.User
import com.leon.be_nobat.domain.repository.AuthRepository
import com.leon.be_nobat.domain.repository.SessionRepository

class AuthRepositoryImpl(
    private val remoteDataSource: PocketBaseClient,
    private val sessionRepository: SessionRepository,
) : AuthRepository {

    override suspend fun loginWithEmail(email: String, password: String): Result<User> =
        remoteDataSource.authWithPassword(email, password).mapCatching { response ->
            require(response.token.isNotBlank()) { "پاسخ ورود فاقد توکن است" }
            sessionRepository.saveToken(response.token)
            response.record.run {
                User(
                    password = this.password,
                    tokenKey = tokenKey,
                    email = this.email,
                    emailVisibility = emailVisibility,
                    verified = verified,
                    name = name,
                    avatar = avatar,
                    mobile = mobile,
                    status = status,
                )
            }
        }
}
