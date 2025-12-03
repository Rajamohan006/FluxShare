package com.rajamohan.fluxshare.domain.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class Device(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val deviceModel: String? = null,
    val osVersion: String? = null
)