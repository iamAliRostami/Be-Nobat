package com.leon.be_nobat.data.remote

import com.leon.be_nobat.data.AppApiImpl
import com.leon.be_nobat.domain.interfaces.IAppApi
import com.leon.be_nobat.domain.interfaces.INetworkManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.auth.providers.*

object NetworkManger : INetworkManager {

    override fun provideHttpClient(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Auth) {
            bearer {
                loadTokens {


                    // اینجا توکن را از TokenManager می‌خوانیم
                    val token =
                        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb2xsZWN0aW9uSWQiOiJwYmNfMzE0MjYzNTgyMyIsImV4cCI6MTc3MjA0NDY1OSwiaWQiOiJ6YXFzN2p6OTIyanNob3kiLCJyZWZyZXNoYWJsZSI6dHJ1ZSwidHlwZSI6ImF1dGgifQ.yiTwkZTMpFaffcUKoJMjxebPkkhVCkVT8zbLjcWt3jo"
                    token =   tokenManager.userToken.first()
                    /*?.let {
                        BearerTokens(accessToken = it, refreshToken = "")
                    }*/
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

    override fun provideApiService(client: HttpClient): IAppApi =
        AppApiImpl(client, Dispatchers.IO)
}