package com.leon.be_nobat.domain.interfaces

/**
 * Application API boundary.
 *
 * Keeping this contract in the domain layer allows callers to depend on API capabilities
 * without knowing that the current implementation uses PocketBase and Ktor.
 */
interface IAppApi : IApiService
