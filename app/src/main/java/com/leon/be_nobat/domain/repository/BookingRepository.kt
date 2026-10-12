package com.leon.be_nobat.domain.repository

import org.json.JSONObject

interface BookingRepository {
    suspend fun request(
        path: String, method: String = "GET", body: JSONObject? = null,
        query: Map<String, String> = emptyMap()
    ): JSONObject

    suspend fun avatar(path: String): ByteArray
}