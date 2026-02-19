package com.trycatchers.hotel.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.BookingRepository
import com.trycatchers.hotel.data.repositories.RoomRepository
import com.trycatchers.hotel.utils.formatDisplayDate
import com.trycatchers.hotel.utils.parseApiDate
import com.trycatchers.hotel.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Representa el estado de la pantalla de pago de la reserva. */
data class BookingPaymentUiState(
    val isLoading: Boolean = true,
    val isPaying: Boolean = false,
    val bookingId: String? = null,
    val room: Room? = null,
    val checkInLabel: String = "",
    val checkOutLabel: String = "",
    val nights: Int = 0,
    val occupants: Int = 0,
    val totalPrice: Double = 0.0,
    val isPaid: Boolean = false,
    val errorMessage: String? = null,
) {
    val formattedTotalPrice: String
        get() = NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(totalPrice)

    val paymentStatusLabel: String
        get() = if (isPaid) "Pagada" else "Pendiente de pago"
}

sealed interface BookingPaymentEvent {
    data object PaymentCompleted : BookingPaymentEvent
}

/**
 * ViewModel de pago de reservas.
 * Orquesta la carga de resumen de pago y la confirmación de pago contra backend.
 */
@HiltViewModel
class BookingPaymentViewModel
@Inject
constructor(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bookingIdArg: String = savedStateHandle["bookingId"] ?: ""

    private val _uiState = MutableStateFlow(BookingPaymentUiState())
    val uiState: StateFlow<BookingPaymentUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BookingPaymentEvent>()
    val events: SharedFlow<BookingPaymentEvent> = _events.asSharedFlow()

    init {
        loadPaymentSummary()
    }

    /**
     * Carga datos de reserva/habitación y construye el estado visual de pago.
     */
    fun loadPaymentSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val booking = bookingRepository.getById(bookingIdArg)
                val room = runCatching { roomRepository.getById(booking.roomId) }.getOrNull()

                val checkIn = parseDate(booking.startDate)
                val checkOut = parseDate(booking.endDate)
                val nights = max(booking.totalNights, 1)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        bookingId = booking.bookingId,
                        room = room,
                        checkInLabel = checkIn?.let(::formatDisplayDate).orEmpty(),
                        checkOutLabel = checkOut?.let(::formatDisplayDate).orEmpty(),
                        nights = nights,
                        occupants = booking.occupants,
                        totalPrice = booking.totalPrice,
                        isPaid = booking.isPaid,
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            error.toUserMessage(
                                defaultMessage =
                                    "No se pudo cargar la información de la reserva"
                            )
                    )
                }
            }
        }
    }

    /**
     * Ejecuta el flujo de pago y actualiza el estado con la reserva resultante.
     */
    fun confirmPayment() {
        val bookingId = _uiState.value.bookingId ?: bookingIdArg
        if (bookingId.isBlank() || _uiState.value.isPaid || _uiState.value.isPaying) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPaying = true, errorMessage = null) }
            try {
                val updatedBooking = bookingRepository.pay(bookingId)
                val room =
                    _uiState.value.room
                        ?: runCatching { roomRepository.getById(updatedBooking.roomId) }
                            .getOrNull()

                val checkIn = parseDate(updatedBooking.startDate)
                val checkOut = parseDate(updatedBooking.endDate)

                _uiState.update {
                    it.copy(
                        isPaying = false,
                        bookingId = updatedBooking.bookingId,
                        room = room,
                        checkInLabel = checkIn?.let(::formatDisplayDate).orEmpty(),
                        checkOutLabel = checkOut?.let(::formatDisplayDate).orEmpty(),
                        nights = max(updatedBooking.totalNights, 1),
                        occupants = updatedBooking.occupants,
                        totalPrice = updatedBooking.totalPrice,
                        isPaid = updatedBooking.isPaid,
                    )
                }

                _events.emit(BookingPaymentEvent.PaymentCompleted)
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isPaying = false,
                        errorMessage =
                            error.toUserMessage(
                                defaultMessage =
                                    "No se pudo procesar el pago. Inténtalo nuevamente"
                            )
                    )
                }
            }
        }
    }

    /**
     * Reinicia el estado local al salir de la pantalla de pago.
     */
    fun reset() {
        _uiState.value = BookingPaymentUiState()
    }

    /**
     * Convierte una fecha API a LocalDate de forma segura.
     */
    private fun parseDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        return parseApiDate(raw)
    }
}
