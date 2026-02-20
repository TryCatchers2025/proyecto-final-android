package com.trycatchers.hotel.data.dtos

import com.squareup.moshi.Json
import com.trycatchers.hotel.data.models.Booking

/**
 * Data Transfer Object (DTO) para Bookings Representa la respuesta JSON de la API de MongoDB Se
 * convierte a modelo de dominio mediante [toDomain]
 */
data class BookingDto(
    @Json(name = "bookingId") val bookingId: String?,
    @Json(name = "userId") val userId: String,
    @Json(name = "roomId") val roomId: String,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "bookingDate") val bookingDate: String?,
    @Json(name = "occupants") val occupants: Int,
    @Json(name = "pricePerNight") val pricePerNight: Double,
    @Json(name = "totalPrice") val totalPrice: Double,
    @Json(name = "discount") val discount: Double = 0.0,
    @Json(name = "totalNights") val totalNights: Int,
    @Json(name = "status") val status: String = "active",
    @Json(name = "isPaid") val isPaid: Boolean = false,
    @Json(name = "checkInNotified") val checkInNotified: Boolean = false,
    @Json(name = "checkOutNotified") val checkOutNotified: Boolean = false
) {
    /** Convierte este DTO a un modelo de dominio */
    fun toDomain() =
        Booking(
            bookingId = bookingId,
            userId = userId,
            roomId = roomId,
            startDate = startDate,
            endDate = endDate,
            bookingDate = bookingDate,
            occupants = occupants,
            pricePerNight = pricePerNight,
            totalPrice = totalPrice,
            discount = discount,
            totalNights = totalNights,
            status = status,
            isPaid = isPaid,
            checkInNotified = checkInNotified,
            checkOutNotified = checkOutNotified
        )
}

/** Extensión para convertir lista de DTOs a modelos */
fun List<BookingDto>.toDomain() = map { it.toDomain() }
