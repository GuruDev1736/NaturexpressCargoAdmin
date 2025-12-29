package com.naturexpresscargo.admin.models

import java.io.Serializable

data class ServiceRequest(
    val requestId: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val pricePerWeight: Double = 0.0,
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val weight: Double = 0.0,
    val totalPrice: Double = 0.0,
    val status: String = "pending", // pending, confirmed, in_transit, delivered, cancelled
    val createdAt: Long = System.currentTimeMillis(),
    // New fields
    val pickupAddress: String = "",
    val deliveryAddress: String = "",
    val cargoType: String = "",
    val cargoDescription: String = "",
    val numberOfPackages: String = "",
    val actualWeightKg: String = "",
    val transportMode: String = "",
    val pickupDate: String = "",
    val contactNumber: String = ""
) : Serializable

