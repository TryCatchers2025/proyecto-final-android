package com.trycatchers.hotel.data.api

import com.trycatchers.hotel.data.dtos.CreateReviewRequest
import com.trycatchers.hotel.data.dtos.ReviewDto
import com.trycatchers.hotel.data.dtos.UpdateReviewRequest
import retrofit2.http.*

/**
 * Servicio de API para gestionar reseñas mediante Retrofit.
 *
 * Mapea los endpoints REST del backend:
 * - `GET /reviews?userId=xxx` → reseñas del usuario autenticado
 * - `GET /reviews?roomId=xxx` → reseñas de una habitación
 * - `GET /reviews/:id`        → una reseña específica
 * - `POST /reviews`           → crear reseña (solo customers)
 * - `PUT /reviews/:id`        → actualizar reseña propia
 * - `DELETE /reviews/:id`     → eliminar reseña propia
 */
interface ReviewService {

    @GET("reviews")
    suspend fun getReviewsByUser(@Query("userId") userId: String): List<ReviewDto>

    @GET("reviews")
    suspend fun getReviewsByRoom(@Query("roomId") roomId: String): List<ReviewDto>

    @GET("reviews/{id}")
    suspend fun getReviewById(@Path("id") id: String): ReviewDto

    @POST("reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): ReviewDto

    @PUT("reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: String,
        @Body request: UpdateReviewRequest,
    ): ReviewDto

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String)
}
