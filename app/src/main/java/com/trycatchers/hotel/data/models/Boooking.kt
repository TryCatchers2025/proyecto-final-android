package com.trycatchers.hotel.data.models

/**
 * Modelo de dominio para Booking Representa la entidad de negocio, completamente independiente de
 * Moshi/JSON
 *
 * @property bookingId ID único de la reserva
 * @property userId ID del usuario que realiza la reserva
 * @property roomId ID de la habitación reservada
 * @property startDate Fecha de inicio de la estancia
 * @property endDate Fecha de fin de la estancia
 * @property bookingDate Fecha en que se realizó la reserva
 * @property occupants Número de ocupantes
 * @property pricePerNight Precio por noche (con descuento aplicado)
 * @property totalPrice Precio total de la estancia
 * @property discount Porcentaje de descuento aplicado
 * @property totalNights Número total de noches
 * @property status Estado de la reserva ('active' o 'canceled')
 * @property isPaid Indica si la reserva ya fue pagada
 * @property checkInNotified Si se envió recordatorio de check-in
 * @property checkOutNotified Si se envió recordatorio de check-out
 */
data class Booking(
    val bookingId: String? = null,
    val userId: String,
    val roomId: String,
    val startDate: String,
    val endDate: String,
    val bookingDate: String? = null,
    val occupants: Int,
    val pricePerNight: Double,
    val totalPrice: Double,
    val discount: Double = 0.0,
    val totalNights: Int,
    val status: String = "active",
    val isPaid: Boolean = false,
    val checkInNotified: Boolean = false,
    val checkOutNotified: Boolean = false
)
