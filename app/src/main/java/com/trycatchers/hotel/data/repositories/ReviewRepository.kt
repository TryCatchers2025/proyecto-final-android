package com.trycatchers.hotel.data.repositories

import com.trycatchers.hotel.data.api.ReviewService
import com.trycatchers.hotel.data.dtos.CreateReviewRequest
import com.trycatchers.hotel.data.dtos.UpdateReviewRequest
import com.trycatchers.hotel.data.dtos.toDomain
import com.trycatchers.hotel.data.models.Review
import javax.inject.Inject

/**
 * Repositorio para gestionar operaciones de reseñas.
 *
 * Convierte DTOs de la API a modelos de dominio y expone operaciones CRUD.
 */
class ReviewRepository @Inject constructor(private val reviewService: ReviewService) {

    /**
     * Obtiene todas las reseñas del usuario con el ID indicado.
     *
     * @param userId ID del usuario cuyas reseñas se desean obtener
     * @return Lista de reseñas del usuario
     */
    suspend fun getByUser(userId: String): List<Review> =
        reviewService.getReviewsByUser(userId).toDomain()

    /**
     * Obtiene todas las reseñas de una habitación.
     *
     * @param roomId ID de la habitación
     * @return Lista de reseñas de la habitación
     */
    suspend fun getByRoom(roomId: String): List<Review> =
        reviewService.getReviewsByRoom(roomId).toDomain()

    /**
     * Obtiene una reseña por su ID.
     *
     * @param id ID de la reseña
     * @return Datos de la reseña
     */
    suspend fun getById(id: String): Review = reviewService.getReviewById(id).toDomain()

    /**
     * Crea una nueva reseña.
     *
     * @param bookingId ID de la reserva a reseñar
     * @param rate Calificación (0.5–5.0)
     * @param comment Comentario (máx. 250 caracteres)
     * @return Reseña creada
     */
    suspend fun create(bookingId: String, rate: Double, comment: String): Review =
        reviewService.createReview(CreateReviewRequest(bookingId, rate, comment)).toDomain()

    /**
     * Actualiza una reseña existente.
     *
     * @param id ID de la reseña
     * @param rate Nueva calificación, o null para no modificar
     * @param comment Nuevo comentario, o null para no modificar
     * @return Reseña actualizada
     */
    suspend fun update(id: String, rate: Double? = null, comment: String? = null): Review =
        reviewService.updateReview(id, UpdateReviewRequest(rate, comment)).toDomain()

    /**
     * Elimina una reseña.
     *
     * @param id ID de la reseña a eliminar
     */
    suspend fun delete(id: String) = reviewService.deleteReview(id)
}
