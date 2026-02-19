package com.trycatchers.hotel.data.models

/**
 * Modelo de dominio para Review.
 *
 * Representa la entidad de negocio de una reseña, completamente independiente de Moshi/JSON.
 *
 * @property reviewId ID único de la reseña
 * @property userId ID del usuario que creó la reseña
 * @property roomId ID de la habitación reseñada
 * @property bookingId ID de la reserva asociada
 * @property rate Calificación numérica (0.5–5.0)
 * @property comment Comentario de la reseña (máx. 250 caracteres)
 * @property createdAt Fecha de creación en formato de la API
 */
data class Review(
    val reviewId: String? = null,
    val userId: String,
    val roomId: String,
    val bookingId: String,
    val rate: Double,
    val comment: String,
    val createdAt: String? = null,
)
