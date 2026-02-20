package com.trycatchers.hotel.data.repositories

import com.trycatchers.hotel.data.api.BookingService
import com.trycatchers.hotel.data.dtos.BookingDto
import com.trycatchers.hotel.data.dtos.CreateBookingRequest
import com.trycatchers.hotel.data.dtos.toDomain
import com.trycatchers.hotel.data.models.Booking
import javax.inject.Inject

/**
 * Repositorio para gestionar operaciones de reservas.
 *
 * Convierte DTOs de la API a modelos de dominio y expone operaciones CRUD.
 */
class BookingRepository @Inject constructor(private val bookingService: BookingService) {

    /**
     * Obtiene todas las reservas del usuario autenticado (filtradas por rol en el servidor).
     *
     * @return Lista de reservas
     */
    suspend fun getAll(): List<Booking> = bookingService.getAllBookings().toDomain()

    /**
     * Obtiene una reserva por su ID.
     *
     * @param id ID de la reserva
     * @return Datos de la reserva
     */
    suspend fun getById(id: String): Booking = bookingService.getBookingById(id).toDomain()

    /**
     * Crea una nueva reserva.
     *
     * @param request Datos mínimos para crear la reserva
     * @return Reserva creada con su ID asignado
     */
    suspend fun create(request: CreateBookingRequest): Booking =
        bookingService.createBooking(request).toDomain()

    /**
     * Actualiza fechas u ocupantes de una reserva existente.
     *
     * @param id ID de la reserva a actualizar
     * @param booking Datos actualizados de la reserva
     * @return Reserva actualizada
     */
    suspend fun update(id: String, booking: Booking): Booking {
        val dto = booking.toDto()
        return bookingService.updateBooking(id, dto).toDomain()
    }

    /**
     * Cancela una reserva (cambia su estado a 'canceled').
     *
     * @param id ID de la reserva a cancelar
     * @return Reserva actualizada con estado cancelado
     */
    suspend fun cancel(id: String): Booking = bookingService.cancelBooking(id).toDomain()

    /**
     * Extiende la fecha de fin de una reserva.
     *
     * @param id ID de la reserva
     * @param newEndDate Nueva fecha de fin en formato DD/MM/YYYY
     * @return Reserva actualizada con la nueva fecha de fin
     */
    suspend fun extend(id: String, newEndDate: String): Booking =
        bookingService.extendBooking(id, mapOf("endDate" to newEndDate)).toDomain()

    /**
     * Marca una reserva como pagada.
     *
     * @param id ID de la reserva
     * @return Reserva actualizada tras el pago
     */
    suspend fun pay(id: String): Booking = bookingService.payBooking(id).toDomain()

    /**
     * Elimina físicamente una reserva (solo administradores).
     *
     * @param id ID de la reserva a eliminar
     */
    suspend fun delete(id: String) = bookingService.deleteBooking(id)
}

/** Extensión para convertir modelo de dominio a DTO. */
fun Booking.toDto() =
    BookingDto(
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
        checkOutNotified = checkOutNotified,
    )
