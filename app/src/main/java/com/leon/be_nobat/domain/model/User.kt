package com.leon.be_nobat.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val password: String,
    val tokenKey: String,
    val email: String?,
    @SerialName("email_visibility") val emailVisibility: Boolean?,
    val verified: Boolean?,
    val name: String,
    val avatar: String?,
    val mobile: String,
    val status: String
) : BaseDomain()