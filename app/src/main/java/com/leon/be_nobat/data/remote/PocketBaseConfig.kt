package com.leon.be_nobat.data.remote

import com.leon.be_nobat.BuildConfig

class PocketBaseConfig {
    companion object {
        val BASE_URL: String =
            if (BuildConfig.DEBUG) "http://10.0.2.2:8090"
            else ""
    }
}