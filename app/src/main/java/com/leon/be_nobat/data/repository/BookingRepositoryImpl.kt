package com.leon.be_nobat.data.repository

import com.leon.be_nobat.data.remote.BeNobatApi
import com.leon.be_nobat.domain.repository.BookingRepository
import org.json.JSONObject

class BookingRepositoryImpl(private val remote: BeNobatApi) : BookingRepository {
    override suspend fun request(
        path: String,
        method: String,
        body: JSONObject?,
        query: Map<String, String>
    ) =
        remote.request(path, method, body, query)

    override suspend fun avatar(path: String) = remote.avatar(path)
}