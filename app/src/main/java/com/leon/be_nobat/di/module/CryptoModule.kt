package com.leon.be_nobat.di.module

import com.leon.be_nobat.data.token.CryptoManager
import org.koin.dsl.module

val cryptoModule = module {
    single { CryptoManager }
}