package com.trycatchers.hotel.data.models

data class User(
    val userId: String? = null,
    val email: String,
    val password: String,
    val name: String
)