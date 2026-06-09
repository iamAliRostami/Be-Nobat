package com.leon.be_nobat.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.leon.be_nobat.R
import com.leon.be_nobat.data.QueueRecord
import com.leon.be_nobat.domain.consts.Services
import com.leon.be_nobat.domain.PocketBaseResponse
import com.leon.be_nobat.data.local.TokenManager
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
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    val tokenManager: TokenManager by inject()


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val splashScreen = installSplashScreen()

        lifecycleScope.launch {
            val token = tokenManager.getToken()
            if (token != null) {
                /*startActivity(Intent(this@MainActivity, HomeActivity::class.java))*/
                setContentView(R.layout.activity_main)
            } else {
                // اگر نداشت، صفحه لاگین را نشان بده
                setContentView(R.layout.activity_login)
                setupLoginLogic()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
            finish()
        }


        /* lifecycleScope.launch {
             try {
                 val allQueues = getQueues()
                 Log.e("size", allQueues.items.size.toString())
             } catch (e: Exception) {
                 // Handle connection errors (e.g., server is offline)
                 Log.e("KtorError", "Failed to connect: ${e.message}")
             }
         }*/
    }

    private fun setupLoginLogic() {
        // اینجا کدهای مربوط به کلیک دکمه‌ها و ارسال موبایل به سرور کاتور را بنویس
    }

    suspend fun getQueues(): PocketBaseResponse<QueueRecord> {
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
            client.get(Services.baseUrl + "/api/collections/access/records")
        return response.body<PocketBaseResponse<QueueRecord>>()
    }
}