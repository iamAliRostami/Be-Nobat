package com.leon.be_nobat.domain.interfaces

/** Provides the domain-facing API without leaking an HTTP client into the domain layer. */
interface INetworkManager {
    val apiService: IAppApi
}
