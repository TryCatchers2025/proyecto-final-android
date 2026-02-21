package com.trycatchers.hotel.data.dtos

import com.squareup.moshi.Json
import com.trycatchers.hotel.data.models.Room

data class RoomDto(
        @Json(name = "_id") val roomId: String?,
        @Json(name = "name") val name: String,
        @Json(name = "type") val type: String,
        @Json(name = "number") val number: Int,
        @Json(name = "pricePerNight") val price: Double,
        @Json(name = "occuped") val occupied: Boolean,
        @Json(name = "occupancyLimit") val limit: Int,
        @Json(name = "offer") val offer: Int,
        @Json(name = "description") val description: String
) {
    fun toDomain() = Room(roomId = roomId, number = number, price = price, type = type, name = name, occupied = occupied, limit = limit, offer = offer, description = description)
}

fun List<RoomDto>.toDomain() = map { it.toDomain() }
