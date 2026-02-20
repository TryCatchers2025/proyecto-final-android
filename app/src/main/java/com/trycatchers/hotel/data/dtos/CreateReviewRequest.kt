package com.trycatchers.hotel.data.dtos

import com.squareup.moshi.Json

/**
 * Carga útil para crear una nueva reseña.
 *
 * Coincide con los campos que el backend espera en `POST /reviews`.
 *
 * @property bookingId ID de la reserva a reseñar
 * @property rate Calificación (0.5–5.0)
 * @property comment Comentario (máx. 250 caracteres)
 */
data class CreateReviewRequest(
    @Json(name = "bookingId") val bookingId: String,
    @Json(name = "rate") val rate: Double,
    @Json(name = "comment") val comment: String,
)
