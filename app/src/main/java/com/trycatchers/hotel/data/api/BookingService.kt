package com.trycatchers.hotel.data.api

import com.trycatchers.hotel.data.dtos.BookingDto
import com.trycatchers.hotel.data.dtos.CreateBookingRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Servicio de API para gestionar reservas mediante Retrofit.
 *
 * Mapea los endpoints REST del backend:
 * - `GET /bookings`              → listado (filtrado por rol en servidor)
 * - `GET /bookings/:id`          → una reserva específica
 * - `POST /bookings`             → crear reserva
 * - `PUT /bookings/:id`          → actualizar reserva (fechas/ocupantes)
 * - `PUT /bookings/:id/cancel`   → cancelar reserva
 * - `PUT /bookings/:id/extend`   → extender fecha de fin
 * - `PUT /bookings/:id/pay`      → marcar como pagada
 * - `DELETE /bookings/:id`       → eliminar físicamente (solo admin)
 */
interface BookingService {

    @GET("bookings")
    suspend fun getAllBookings(): List<BookingDto>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: String): BookingDto

    @POST("bookings")
    suspend fun createBooking(@Body booking: CreateBookingRequest): BookingDto

    @PUT("bookings/{id}")
    suspend fun updateBooking(@Path("id") id: String, @Body booking: BookingDto): BookingDto

    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: String): BookingDto

    @PUT("bookings/{id}/extend")
    suspend fun extendBooking(
        @Path("id") id: String,
        @Body body: Map<String, String>,
    ): BookingDto

    @PUT("bookings/{id}/pay")
    suspend fun payBooking(@Path("id") id: String): BookingDto

    @DELETE("bookings/{id}")
    suspend fun deleteBooking(@Path("id") id: String)
}
