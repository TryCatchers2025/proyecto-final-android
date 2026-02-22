package com.trycatchers.hotel.data.dtos

import com.squareup.moshi.Json
import com.trycatchers.hotel.data.models.User

data class UserDto(
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "email") val email: String = "",
    @Json(name = "firstName") val firstName: String = "",
    @Json(name = "lastName") val lastName: String = "",
    @Json(name = "birthDate") val birthDate: String = "",
    @Json(name = "gender") val gender: String = "",
    @Json(name = "dni") val dni: String = "",
    @Json(name = "city") val city: String? = null,
    @Json(name = "vip") val vip: Boolean? = null,
    @Json(name = "photo") val photo: String? = null,
    @Json(name = "password") val password: String = ""
) {
    fun toDomain() = User(
        userId = userId,
        email = email,
        password = password,
        firstName = firstName,
        lastName = lastName,
        vip = vip == true,
        city = city,
        gender = gender,
        birthDate = birthDate,
        dni = dni,
        photo = photo
    )
}

fun List<UserDto>.toDomain() = map { it.toDomain() }
