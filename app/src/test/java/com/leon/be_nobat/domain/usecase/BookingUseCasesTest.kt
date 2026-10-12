package com.leon.be_nobat.domain.usecase
import com.leon.be_nobat.domain.repository.BookingRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class BookingUseCasesTest {
    @Test fun optionsContinueBeyondTheFirstHundredRows() = runBlocking {
        val pages = mutableListOf<Int>()
        val repository = object : BookingRepository {
            override suspend fun request(path: String, method: String, body: JSONObject?, query: Map<String, String>): JSONObject {
                pages.add(query.getValue("page").toInt())
                val first = (pages.last() - 1) * 100
                val items = org.json.JSONArray()
                for (id in first until minOf(first + 100, 205)) items.put(JSONObject().put("id", id.toString()))
                return JSONObject().put("items", items).put("total", 205)
            }
            override suspend fun avatar(path: String) = byteArrayOf()
        }
        val result = BookingUseCases(repository).all("admin/branches")
        assertEquals(listOf(1, 2, 3), pages)
        assertEquals(205, result.length())
        assertEquals("204", result.getJSONObject(204).getString("id"))
    }
}
