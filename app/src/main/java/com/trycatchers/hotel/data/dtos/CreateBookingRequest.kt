package com.trycatchers.hotel.data.dtos

import com.squareup.moshi.Json

/**
 * Representa la carga útil mínima para crear una reserva desde la app móvil. Coincide con los
 * campos que el backend espera recibir en el endpoint POST /bookings.
 */
data class CreateBookingRequest(
    @Json(name = "roomId") val roomId: String,
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "occupants") val occupants: Int,
    @Json(name = "discount") val discount: Double? = null,
    @Json(name = "userId") val userId: String? = null,
)
