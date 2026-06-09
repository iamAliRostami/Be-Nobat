package com.leon.be_nobat.domain.consts

import com.leon.be_nobat.BuildConfig

class Services {
    companion object {
        val baseUrl: String =
            if (BuildConfig.DEBUG) "http://10.0.2.2:8090"
            else ""
    }
}