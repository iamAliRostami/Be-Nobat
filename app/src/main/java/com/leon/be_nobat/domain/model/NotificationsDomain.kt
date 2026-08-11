package com.leon.be_nobat.domain.ttt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationsDomain(
    @SerialName("user_id") val userId: String?,
    val title: String?,
    val body: String?,
    val type: String?,
    @SerialName("reference_id") val referenceId: String?,
    val status: String?,
    @SerialName("sent_at") val sentAt: String
) : BaseDomain()