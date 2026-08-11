package com.leon.be_nobat.domain.ttt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BranchMembershipDomain(
    @SerialName("branch_id") val branchId: String?,
    @SerialName("user_id") val userId: String?,
    val roles: String?,
    val status: String
) : BaseDomain()