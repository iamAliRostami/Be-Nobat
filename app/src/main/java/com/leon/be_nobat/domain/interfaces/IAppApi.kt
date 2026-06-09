package com.leon.be_nobat.domain.interfaces

import com.leon.be_nobat.domain.model.User

interface IAppApi {
    suspend fun fetchUsers(): List<User>
}