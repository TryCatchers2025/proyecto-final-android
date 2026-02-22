package com.trycatchers.hotel.data.models

data class User(
    val userId: String? = null,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val vip: Boolean,
    val birthDate: String,
    val gender: String,
    val city: String?,
    val dni: String,
    val photo: String? = null
)