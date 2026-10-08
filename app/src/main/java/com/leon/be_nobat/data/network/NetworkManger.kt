package com.leon.be_nobat.data.network

/*import android.R.attr.level*/
import com.leon.be_nobat.domain.interfaces.INetworkManager
import com.leon.be_nobat.domain.interfaces.ITokenManager
import io.ktor.client.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.*

import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

import io.ktor.client.engine.android.*
class NetworkManager /*@Inject*/ constructor(
    private val tokenManager: ITokenManager
) : INetworkManager {


    override fun provideApiService(client: HttpClient) {
        TODO("Not yet implemented")
    }

    override fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {          // Android engine
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            /*install(ContentNegotiation) {
                json(json)
            }*/

            // Optional: Logging (use LogLevel.BODY only in debug)
            /*install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
            }*/
            /*install(Logging) {
                level = LogLevel.ALL
            }*/
            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }
                }
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        // اینجا توکن را از TokenManager می‌خوانیم
                        val token = tokenManager.getToken()
                        /*"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb2xsZWN0aW9uSWQiOiJwYmNfMzE0MjYzNTgyMyIsImV4cCI6MTc3MjA0NDY1OSwiaWQiOiJ6YXFzN2p6OTIyanNob3kiLCJyZWZyZXNoYWJsZSI6dHJ1ZSwidHlwZSI6ImF1dGgifQ.yiTwkZTMpFaffcUKoJMjxebPkkhVCkVT8zbLjcWt3jo"*/
                        if (!token.isNullOrBlank()) {
                            BearerTokens(
                                token,
                                ""
                            ) // توکن دوم برای Refresh Token است که در پکت‌بیس فعلاً نیازی نیست
                        } else null
                    }
                }
            }
        }
    }
}