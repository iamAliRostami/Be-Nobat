package com.leon.be_nobat.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val password: String = "",
    val tokenKey: String = "",
    val email: String? = null,
    @SerialName("emailVisibility") val emailVisibility: Boolean? = null,
    val verified: Boolean? = null,
    val name: String = "",
    val avatar: String? = null,
    val mobile: String = "",
    val status: String = "",
) : BaseDomain()
