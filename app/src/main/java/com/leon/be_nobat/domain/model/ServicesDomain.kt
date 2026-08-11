package com.leon.be_nobat.domain.ttt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServicesDomain(
    @SerialName("business_id") val businessId: String?,
    @SerialName("category_id") val categoryId: String?,
    val name: String?,
    val description: String?,
    val duration: Double,
    val status: Boolean,
    @SerialName("base_price") val basePrice: String,
    @SerialName("deleted_at") val deletedAt: String?,
    @SerialName("deleted_by") val deletedBy: String?
) : BaseDomain()