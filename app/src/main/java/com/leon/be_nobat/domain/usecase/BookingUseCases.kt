package com.leon.be_nobat.domain.usecase

import com.leon.be_nobat.domain.repository.BookingRepository
import org.json.JSONArray
import org.json.JSONObject

/** Coordinates paginated reads. Server authorization and validation remain authoritative. */
class BookingUseCases(private val repository: BookingRepository) {
    suspend fun request(
        path: String, method: String = "GET", body: JSONObject? = null,
        query: Map<String, String> = emptyMap()
    ) = repository.request(path, method, body, query)

    suspend fun avatar(path: String) = repository.avatar(path)
    suspend fun all(path: String, query: Map<String, String> = emptyMap()): JSONArray {
        val result = JSONArray()
        var page = 1
        do {
            val response = repository.request(
                path,
                query = query + mapOf("page" to page.toString(), "pageSize" to "100")
            )
            val items = response.optJSONArray("items") ?: break
            for (index in 0 until items.length()) result.put(items.getJSONObject(index))
            if (items.length() == 0 || result.length() >= response.optInt(
                    "total",
                    result.length()
                )
            ) break
            page++
        } while (page <= 1000)
        return result
    }
}