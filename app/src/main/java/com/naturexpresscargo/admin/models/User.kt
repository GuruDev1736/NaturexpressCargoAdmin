package com.naturexpresscargo.admin.models

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "ROLE_USER",
    val createdAt: Long = System.currentTimeMillis()
)

