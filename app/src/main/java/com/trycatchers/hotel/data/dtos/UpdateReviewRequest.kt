package com.trycatchers.hotel.data.dtos

import com.squareup.moshi.Json

/**
 * Carga útil para actualizar una reseña existente.
 *
 * Coincide con los campos que el backend acepta en `PUT /reviews/:id`.
 * Ambos campos son opcionales; se envían solo los que se desean modificar.
 *
 * @property rate Nueva calificación (0.5–5.0), o null para no modificar
 * @property comment Nuevo comentario (máx. 250 caracteres), o null para no modificar
 */
data class UpdateReviewRequest(
    @Json(name = "rate") val rate: Double? = null,
    @Json(name = "comment") val comment: String? = null,
)
