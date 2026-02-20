package com.trycatchers.hotel.data.dtos

import com.squareup.moshi.Json
import com.trycatchers.hotel.data.models.Room

data class RoomDto(
    @Json(name = "_id") val id: String? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "type") val type: String = "",
    @Json(name = "number") val number: Int = 0,
    @Json(name = "description") val description: String? = null,
    @Json(name = "mainImage") val mainImage: String? = null,
    @Json(name = "extraImages") val extraImages: List<String>? = null,
    @Json(name = "pricePerNight") val pricePerNight: Double = 0.0,
    @Json(name = "rate") val rate: Double? = null,
    @Json(name = "occupancyLimit") val occupancyLimit: Int = 1,
    @Json(name = "isAvailable") val isAvailable: Boolean? = null,
    @Json(name = "occuped") val occuped: Boolean? = null,
    @Json(name = "cradle") val cradle: Boolean? = null,
    @Json(name = "extraBed") val extraBed: Boolean? = null,
    @Json(name = "offer") val offerPercentage: Double? = null,
    @Json(name = "extras") val extras: List<String>? = null
) {
    fun toDomain(): Room =
        Room(
            id = id ?: "",
            name = name,
            number = number,
            type = type,
            description = description,
            mainImage = mainImage,
            extraImages = extraImages ?: emptyList(),
            pricePerNight = pricePerNight,
            rate = rate,
            occupancyLimit = occupancyLimit,
            isAvailable = isAvailable ?: !(occuped ?: false),
            hasCradle = cradle ?: false,
            hasExtraBed = extraBed ?: false,
            offerPercentage = offerPercentage,
            extras = extras ?: emptyList()
        )
}

fun List<RoomDto>.toDomain(): List<Room> = map { it.toDomain() }
