package com.leon.be_nobat.domain.ttt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessesDomain(
    val name: String?,
    val description: String?,
    val logo: String?,
    val phone: String?,
    @SerialName("owner_user_id") val ownerUserId: String?,
    val status: String,
    @SerialName("deleted_at") val deletedAt: String?,
    @SerialName("deleted_by") val deletedBy: String?
) : BaseDomain()