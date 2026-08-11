package com.leon.be_nobat.domain.ttt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RolePermissionsDomain(
    @SerialName("role_id") val roleId: String,
    @SerialName("permission_id") val permissionId: String,
    val status: String?
) : BaseDomain()