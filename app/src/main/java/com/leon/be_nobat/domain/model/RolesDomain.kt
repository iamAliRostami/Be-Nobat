package com.leon.be_nobat.domain.ttt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RolesDomain(
    val title: String,
    @SerialName("title_fa") val titleFa: String,
    val scope: String?,
    val code: String?,
    val status: String?,
    @SerialName("deleted_at") val deletedAt: String?,
    @SerialName("deleted_by") val deletedBy: String?
) : BaseDomain()