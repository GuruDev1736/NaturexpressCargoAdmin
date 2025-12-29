package com.naturexpresscargo.admin.models

data class ContactEnquiry(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val numberOfPackages: String = "",
    val itemWeight: String = "",
    val fromLocation: String = "",
    val toLocation: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)

