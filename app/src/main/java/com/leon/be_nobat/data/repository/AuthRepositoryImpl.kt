package com.leon.be_nobat.data.repository

import com.leon.be_nobat.data.local.TokenManager
import com.leon.be_nobat.data.remote.PocketBaseClient
import com.leon.be_nobat.data.remote.PocketBaseException
import com.leon.be_nobat.domain.model.AuthException
import com.leon.be_nobat.domain.model.User
import com.leon.be_nobat.domain.repository.AuthRepository
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException

class AuthRepositoryImpl(
    private val remoteDataSource: PocketBaseClient,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override suspend fun login(identity: String, password: String): Result<User> {
        return remoteDataSource.authWithPassword(identity, password)
            .mapCatching { session ->
                tokenManager.save(session.token)
                session.record
            }
            .recoverCatching { throw it.toAuthException() }
    }

    private fun Throwable.toAuthException(): AuthException = when (this) {
        is AuthException -> this
        is HttpRequestTimeoutException -> AuthException.RequestTimedOut
        is IOException -> AuthException.NetworkUnavailable
        is PocketBaseException -> when (statusCode) {
            400, 401, 403, 404 -> AuthException.InvalidCredentials
            408 -> AuthException.RequestTimedOut
            429 -> AuthException.TooManyRequests
            in 500..599 -> AuthException.ServiceUnavailable
            else -> AuthException.Unexpected
        }
        else -> AuthException.Unexpected
    }
}
