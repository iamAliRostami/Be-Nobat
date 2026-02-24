package com.leon.be_nobat.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.leon.be_nobat.R
import com.leon.be_nobat.data.consts.Services
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lifecycleScope.launch {
            try {
                val allQueues = getQueues()
                Log.e("size", allQueues.items.size.toString())
            } catch (e: Exception) {
                // Handle connection errors (e.g., server is offline)
                Log.e("KtorError", "Failed to connect: ${e.message}")
            }
        }
    }

    suspend fun getQueues(): PocketBaseResponse<QueueRecord> {
        val baseUrl: String
        baseUrl = if (true)
            "s"
        else "a"
        val client = HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        // اینجا توکن را از TokenManager می‌خوانیم
                        val token =
                            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb2xsZWN0aW9uSWQiOiJwYmNfMzE0MjYzNTgyMyIsImV4cCI6MTc3MjA0NDY1OSwiaWQiOiJ6YXFzN2p6OTIyanNob3kiLCJyZWZyZXNoYWJsZSI6dHJ1ZSwidHlwZSI6ImF1dGgifQ.yiTwkZTMpFaffcUKoJMjxebPkkhVCkVT8zbLjcWt3jo"
                        if (token != null) {
                            BearerTokens(
                                token,
                                ""
                            ) // توکن دوم برای Refresh Token است که در پکت‌بیس فعلاً نیازی نیست
                        } else null
                    }
                }
            }
        }
        val response: HttpResponse =
            client.get(Services.baseUrl +"/api/collections/access/records")
        return response.body<PocketBaseResponse<QueueRecord>>()
    }

    @Serializable
    data class PocketBaseResponse<T>(
        val page: Int,
        val perPage: Int,
        val totalItems: Int,
        val totalPages: Int,
        val items: List<T> // لیست اصلی نوبت‌های شما اینجا قرار دارد
    )

    @Serializable
    data class QueueRecord(
        val id: String,
        /*val collectionId: String,
        val customerName: String,
        val status: String,*/
        val title: String
        // بقیه فیلدهایی که در PocketBase تعریف کرده‌اید
    )
}