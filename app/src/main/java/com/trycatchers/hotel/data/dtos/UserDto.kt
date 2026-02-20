package com.trycatchers.hotel.data.dtos

import com.squareup.moshi.Json
import com.trycatchers.hotel.data.models.User

data class UserDto(
    @Json(name = "userId")
    val userId: String?,
    @Json(name = "email")
    val email: String,
    @Json(name = "password")
    val password: String,
    @Json(name = "name")
    val name: String
) {
    fun toDomain() = User(
        userId = userId,
        email = email,
        password = password,
        name = name
    )
}

fun List<UserDto>.toDomain() = map { it.toDomain() }
