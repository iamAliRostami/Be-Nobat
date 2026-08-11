package com.leon.be_nobat.domain.ttt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersDomain(
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