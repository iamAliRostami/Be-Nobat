package com.leon.be_nobat.data

import com.leon.be_nobat.data.remote.PocketBaseClient
import com.leon.be_nobat.data.remote.UserDto
import com.leon.be_nobat.domain.interfaces.IAppApi
import com.leon.be_nobat.domain.model.User

/** PocketBase-backed implementation of the domain API boundary. */
class AppApiImpl(
    private val client: PocketBaseClient,
) : IAppApi {
    override suspend fun fetchUsers(): Result<List<User>> =
        client.list<UserDto>(collection = USERS_COLLECTION).map { response ->
            response.items.map(UserDto::toDomain)
        }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
