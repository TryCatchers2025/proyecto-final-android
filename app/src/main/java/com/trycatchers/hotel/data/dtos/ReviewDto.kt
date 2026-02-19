package com.trycatchers.hotel.data.dtos

import com.squareup.moshi.Json
import com.trycatchers.hotel.data.models.Review

/**
 * Data Transfer Object (DTO) para Reviews.
 *
 * Representa la respuesta JSON de la API. Se convierte a modelo de dominio mediante [toDomain].
 */
data class ReviewDto(
    @Json(name = "_id") val reviewId: String?,
    @Json(name = "userId") val userId: String,
    @Json(name = "roomId") val roomId: String,
    @Json(name = "bookingId") val bookingId: String,
    @Json(name = "rate") val rate: Double,
    @Json(name = "comment") val comment: String,
    @Json(name = "createdAt") val createdAt: String? = null,
) {
    /** Convierte este DTO a un modelo de dominio. */
    fun toDomain() = Review(
        reviewId = reviewId,
        userId = userId,
        roomId = roomId,
        bookingId = bookingId,
        rate = rate,
        comment = comment,
        createdAt = createdAt,
    )
}

/** Extensión para convertir lista de DTOs a modelos de dominio. */
fun List<ReviewDto>.toDomain() = map { it.toDomain() }
