package com.trycatchers.hotel.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.dtos.CreateBookingRequest
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.BookingRepository
import com.trycatchers.hotel.data.repositories.RoomRepository
import com.trycatchers.hotel.data.repositories.SessionRepository
import com.trycatchers.hotel.utils.formatApiDate
import com.trycatchers.hotel.utils.formatDisplayDate
import com.trycatchers.hotel.utils.millisToLocalDate
import com.trycatchers.hotel.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import kotlin.math.max

data class BookingSummaryUiState(
    val isLoading: Boolean = true,
    val isCreatingBooking: Boolean = false,
    val room: Room? = null,
    val occupants: Int = 0,
    val nights: Int = 0,
    val pricePerNight: Double = 0.0,
    val totalPrice: Double = 0.0,
    val startDateLabel: String = "",
    val endDateLabel: String = "",
    val errorMessage: String? = null,
    val discount: Double = 0.0,
    val isVip: Boolean = false,
) {
    val formattedPricePerNight: String
        get() = formatCurrency(pricePerNight)

    val formattedTotalPrice: String
        get() = formatCurrency(totalPrice)

    companion object {
        private fun formatCurrency(value: Double): String =
            NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(value)
    }
}

sealed interface BookingSummaryEvent {
    data class NavigateToPayment(val bookingId: String) : BookingSummaryEvent
}

/**
 * ViewModel para la pantalla de resumen de reserva.
 *
 * Carga los datos de la habitación, calcula el precio estimado para el preview y crea la reserva en
 * el backend al confirmar. El precio definitivo siempre lo calcula el servidor; el cálculo local es
 * solo informativo.
 *
 * El userId se omite intencionalmente del request: el backend lo extrae de la sesión JWT para
 * clientes, por lo que enviarlo desde el cliente es redundante e introduce un vector de
 * manipulación.
 */
@HiltViewModel
class BookingSummaryViewModel
@Inject
constructor(
    private val roomRepository: RoomRepository,
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val roomId: String = savedStateHandle.get<String>("roomId") ?: ""
    private val startDateMillis: Long = savedStateHandle.getLongCompat("startDateMillis")
    private val endDateMillis: Long = savedStateHandle.getLongCompat("endDateMillis")
    private val occupants: Int = savedStateHandle.getIntCompat("occupants", defaultValue = 1)

    private val _uiState = MutableStateFlow(BookingSummaryUiState(occupants = occupants))
    val uiState: StateFlow<BookingSummaryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BookingSummaryEvent>()
    val events: SharedFlow<BookingSummaryEvent> = _events.asSharedFlow()

    private var createdBookingId: String? = null

    init {
        loadRoomDetails()
    }

    /**
     * Carga detalles de habitación y calcula el resumen económico previo a la compra.
     */
    private fun loadRoomDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val room = roomRepository.getById(roomId)
                val startDate = millisToLocalDate(startDateMillis)
                val endDate = millisToLocalDate(endDateMillis)
                val nights = max(1, ChronoUnit.DAYS.between(startDate, endDate).toInt())
                val user = sessionRepository.currentUser.value
                val isVip = user?.vip == true
                val offer = room.offerPercentage ?: 0.0
                val totalDiscount = offer + if (isVip) 10.0 else 0.0
                val pricePerNight = calculateNightlyRate(room.pricePerNight, totalDiscount)
                val totalPrice = pricePerNight * nights

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        room = room,
                        nights = nights,
                        pricePerNight = pricePerNight,
                        totalPrice = totalPrice,
                        startDateLabel = formatDisplayDate(startDate),
                        endDateLabel = formatDisplayDate(endDate),
                        discount = totalDiscount,
                        isVip = isVip
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            error.toUserMessage(
                                defaultMessage =
                                    "No se pudo cargar la información de la habitación"
                            )
                    )
                }
            }
        }
    }

    /**
     * Reintenta la carga del resumen de reserva.
     */
    fun retryLoad() {
        loadRoomDetails()
    }

    /**
     * Valida datos de cliente, crea la reserva y navega al flujo de pago.
     */
    fun proceedToPayment() {
        val currentState = _uiState.value
        val bookingId = createdBookingId

        if (bookingId != null) {
            navigateToPayment(bookingId)
            return
        }

        if (currentState.room == null || currentState.isCreatingBooking || currentState.isLoading) {
            return
        }

        val startDate = millisToLocalDate(startDateMillis)
        val endDate = millisToLocalDate(endDateMillis)
        val today = LocalDate.now()

        if (!startDate.isAfter(today.minusDays(1))) {
            _uiState.update {
                it.copy(errorMessage = "La fecha de entrada no puede ser en el pasado")
            }
            return
        }
        if (!endDate.isAfter(startDate)) {
            _uiState.update {
                it.copy(errorMessage = "La fecha de salida debe ser posterior a la de entrada")
            }
            return
        }
        if (occupants <= 0) {
            _uiState.update { it.copy(errorMessage = "El número de huéspedes debe ser mayor que 0") }
            return
        }
        val limit = currentState.room.occupancyLimit
        if (occupants > limit) {
            _uiState.update {
                it.copy(errorMessage = "Esta habitación admite un máximo de $limit huéspedes")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBooking = true, errorMessage = null) }
            try {
                val request = buildCreateBookingRequest()
                val booking = bookingRepository.create(request)
                val newBookingId = booking.bookingId

                if (newBookingId.isNullOrBlank()) {
                    throw IllegalStateException("La reserva no generó un identificador válido")
                }

                createdBookingId = newBookingId
                _uiState.update { it.copy(isCreatingBooking = false) }
                navigateToPayment(newBookingId)
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingBooking = false,
                        errorMessage =
                            error.toUserMessage(
                                defaultMessage =
                                    "No se pudo crear la reserva. Inténtalo de nuevo"
                            )
                    )
                }
            }
        }
    }

    /**
     * Emite evento de navegación hacia la pantalla de pago para una reserva creada.
     */
    private fun navigateToPayment(bookingId: String) {
        viewModelScope.launch { _events.emit(BookingSummaryEvent.NavigateToPayment(bookingId)) }
    }

    /**
     * Construye el request de creación de reserva.
     *
     * El userId se omite: el backend lo extrae de la sesión JWT para clientes.
     * El precio no se envía: el backend lo calcula de forma autoritativa.
     */
    private fun buildCreateBookingRequest(): CreateBookingRequest {
        return CreateBookingRequest(
            roomId = roomId,
            startDate = formatApiDate(startDateMillis),
            endDate = formatApiDate(endDateMillis),
            occupants = occupants,
        )
    }

    /**
     * Calcula el precio por noche aplicando oferta de habitación si existe.
     */
    private fun calculateNightlyRate(basePrice: Double, offerPercentage: Double): Double {
        return if (offerPercentage <= 0.0) {
            basePrice
        } else {
            val discount = 1 - offerPercentage / 100.0
            (basePrice * discount).let { kotlin.math.round(it * 100) / 100.0 }
        }
    }
}

/**
 * Obtiene un Long desde SavedStateHandle tolerando formatos heterogéneos.
 */
private fun SavedStateHandle.getLongCompat(key: String, defaultValue: Long = 0L): Long {
    val value = runCatching { get<Any?>(key) }.getOrNull()
    return when (value) {
        is Long -> value
        is Int -> value.toLong()
        is Number -> value.toLong()
        is String -> value.toLongOrNull() ?: defaultValue
        else -> defaultValue
    }
}

/**
 * Obtiene un Int desde SavedStateHandle tolerando formatos heterogéneos.
 */
private fun SavedStateHandle.getIntCompat(key: String, defaultValue: Int = 0): Int {
    val value = runCatching { get<Any?>(key) }.getOrNull()
    return when (value) {
        is Int -> value
        is Long -> value.toInt()
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: defaultValue
        else -> defaultValue
    }
}
