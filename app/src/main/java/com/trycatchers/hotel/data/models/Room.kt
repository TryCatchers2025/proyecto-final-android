package com.trycatchers.hotel.data.models

data class Room(
    val roomId: String? = null,
    val name: String,
    val type: String,
    val number: Int,
    val price: Double,
    val occupied: Boolean,
    val limit: Int,
    val offer: Int,
    val description: String
)