package com.naturexpresscargo.admin.models

import java.io.Serializable

data class Service(
    val serviceId: String = "",
    val serviceName: String = "",
    val pricePerWeight: Double = 0.0,
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val active: Boolean = true
) : Serializable

