package com.leon.be_nobat.data.remote

import com.leon.be_nobat.domain.interfaces.IAppApi
import com.leon.be_nobat.domain.interfaces.INetworkManager

/**
 * Compatibility facade for callers that consume a network manager.
 *
 * HTTP construction belongs to the DI composition root; this class only exposes the
 * domain-facing API and therefore does not leak Ktor types or create hidden dependencies.
 */
class NetworkManger(
    override val apiService: IAppApi,
) : INetworkManager
