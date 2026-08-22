package com.leon.be_nobat.domain.interfaces

import com.leon.be_nobat.domain.model.User

/** Domain-facing operations exposed by the application's remote API. */
interface IApiService {
    suspend fun fetchUsers(): Result<List<User>>
}
