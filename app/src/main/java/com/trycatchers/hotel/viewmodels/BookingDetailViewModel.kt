package com.trycatchers.hotel.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.models.Booking
import com.trycatchers.hotel.data.models.Review
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.BookingRepository
import com.trycatchers.hotel.data.repositories.ReviewRepository
import com.trycatchers.hotel.data.repositories.RoomRepository
import com.trycatchers.hotel.utils.formatApiDate
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

/** Estado de la pantalla de detalle de reserva. */
data class BookingDetailUiState(
    val isLoading: Boolean = true,
    val isCanceling: Boolean = false,
    val isExtending: Boolean = false,
    val isPaying: Boolean = false,
    val booking: Booking? = null,
    val room: Room? = null,
    val review: Review? = null,
    val checkInLabel: String = "",
    val checkOutLabel: String = "",
    val nights: Int = 0,
    val errorMessage: String? = null,
    // Extend dialog state
    val showExtendDialog: Boolean = false,
    val extendEndDateMillis: Long? = null,
    val extendError: String? = null,
    // Review dialog state
    val showReviewDialog: Boolean = false,
    val reviewRate: Double = 5.0,
    val reviewComment: String = "",
    val reviewError: String? = null,
    val isSavingReview: Boolean = false,
    val isDeletingReview: Boolean = false,
) {
    val formattedTotalPrice: String
        get() =
            NumberFormat.getCurrencyInstance(Locale("es", "ES"))
                .format(booking?.totalPrice ?: 0.0)

    val isCanceled: Boolean
        get() = booking?.status == "canceled"

    val isPaid: Boolean
        get() = booking?.isPaid == true

    /** La reserva puede cancelarse si está activa y no pagada. */
    val canCancel: Boolean
        get() = !isCanceled && !isPaid

    /** La reserva puede extenderse si está activa. */
    val canExtend: Boolean
        get() = !isCanceled

    /** La reserva puede pagarse si está activa y no pagada. */
    val canPay: Boolean
        get() = !isCanceled && !isPaid

    /** Se puede dejar reseña si la reserva ya finalizó y no está cancelada. */
    val canReview: Boolean
        get() {
            if (isCanceled) return false
            val end = parseApiDate(booking?.endDate) ?: return false
            return !LocalDate.now().isBefore(end)
        }

    val hasReview: Boolean
        get() = review != null
}

/** Eventos de navegación emitidos por [BookingDetailViewModel]. */
sealed interface BookingDetailEvent {
    data object NavigateToPayment : BookingDetailEvent
    data object BookingCanceled : BookingDetailEvent
}

/**
 * ViewModel para la pantalla de detalle de reserva.
 *
 * Gestiona la carga de datos, cancelación, extensión, pago y reseñas de una reserva.
 */
@HiltViewModel
class BookingDetailViewModel
@Inject
constructor(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository,
    private val reviewRepository: ReviewRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bookingId: String = savedStateHandle["bookingId"] ?: ""

    private val _uiState = MutableStateFlow(BookingDetailUiState())
    val uiState: StateFlow<BookingDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BookingDetailEvent>()
    val events: SharedFlow<BookingDetailEvent> = _events.asSharedFlow()

    init {
        load()
    }

    /** Carga la reserva, la habitación y la reseña asociada. */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val booking = bookingRepository.getById(bookingId)
                val room = runCatching { roomRepository.getById(booking.roomId) }.getOrNull()
                val review = runCatching {
                    reviewRepository.getByUser(booking.userId)
                        .firstOrNull { it.bookingId == bookingId }
                }.getOrNull()

                val checkIn = parseApiDate(booking.startDate)
                val checkOut = parseApiDate(booking.endDate)
                val nights = max(booking.totalNights, 1)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        booking = booking,
                        room = room,
                        review = review,
                        checkInLabel = checkIn?.let(::formatDisplayDate).orEmpty(),
                        checkOutLabel = checkOut?.let(::formatDisplayDate).orEmpty(),
                        nights = nights,
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            error.toUserMessage("No se pudo cargar la información de la reserva"),
                    )
                }
            }
        }
    }

    // ── Cancel ──────────────────────────────────────────────────────────────

    /** Cancela la reserva actual. */
    fun cancelBooking() {
        if (_uiState.value.isCanceling || !_uiState.value.canCancel) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCanceling = true, errorMessage = null) }
            try {
                val updated = bookingRepository.cancel(bookingId)
                _uiState.update { it.copy(isCanceling = false, booking = updated) }
                _events.emit(BookingDetailEvent.BookingCanceled)
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isCanceling = false,
                        errorMessage = error.toUserMessage("No se pudo cancelar la reserva"),
                    )
                }
            }
        }
    }

    // ── Extend ──────────────────────────────────────────────────────────────

    /** Abre el diálogo de extensión. */
    fun openExtendDialog() {
        _uiState.update { it.copy(showExtendDialog = true, extendError = null) }
    }

    /** Cierra el diálogo de extensión. */
    fun dismissExtendDialog() {
        _uiState.update {
            it.copy(showExtendDialog = false, extendEndDateMillis = null, extendError = null)
        }
    }

    /** Actualiza la nueva fecha de fin seleccionada en el diálogo de extensión. */
    fun setExtendEndDate(millis: Long) {
        _uiState.update { it.copy(extendEndDateMillis = millis, extendError = null) }
    }

    /** Confirma la extensión con la fecha seleccionada. */
    fun confirmExtend() {
        val millis = _uiState.value.extendEndDateMillis
        if (millis == null) {
            _uiState.update { it.copy(extendError = "Selecciona una nueva fecha de salida") }
            return
        }

        val currentEndDate = parseApiDate(_uiState.value.booking?.endDate)
        val newEndDate = java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()

        if (currentEndDate != null && !newEndDate.isAfter(currentEndDate)) {
            _uiState.update {
                it.copy(extendError = "La nueva fecha debe ser posterior a la fecha de salida actual")
            }
            return
        }

        val newEndDateStr = formatApiDate(millis)

        viewModelScope.launch {
            _uiState.update { it.copy(isExtending = true, extendError = null) }
            try {
                val updated = bookingRepository.extend(bookingId, newEndDateStr)
                val checkOut = parseApiDate(updated.endDate)
                _uiState.update {
                    it.copy(
                        isExtending = false,
                        showExtendDialog = false,
                        extendEndDateMillis = null,
                        booking = updated,
                        checkOutLabel = checkOut?.let(::formatDisplayDate).orEmpty(),
                        nights = max(updated.totalNights, 1),
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isExtending = false,
                        extendError = error.toUserMessage("No se pudo extender la reserva"),
                    )
                }
            }
        }
    }

    // ── Pay ─────────────────────────────────────────────────────────────────

    /** Navega a la pantalla de pago. */
    fun navigateToPayment() {
        viewModelScope.launch { _events.emit(BookingDetailEvent.NavigateToPayment) }
    }

    // ── Review ──────────────────────────────────────────────────────────────

    /** Abre el diálogo de reseña (crear o editar). */
    fun openReviewDialog() {
        val existing = _uiState.value.review
        _uiState.update {
            it.copy(
                showReviewDialog = true,
                reviewRate = existing?.rate ?: 5.0,
                reviewComment = existing?.comment ?: "",
                reviewError = null,
            )
        }
    }

    /** Cierra el diálogo de reseña. */
    fun dismissReviewDialog() {
        _uiState.update { it.copy(showReviewDialog = false, reviewError = null) }
    }

    /** Actualiza la calificación en el formulario de reseña. */
    fun setReviewRate(rate: Double) {
        _uiState.update { it.copy(reviewRate = rate, reviewError = null) }
    }

    /** Actualiza el comentario en el formulario de reseña. */
    fun setReviewComment(comment: String) {
        _uiState.update { it.copy(reviewComment = comment, reviewError = null) }
    }

    /** Guarda la reseña (crea o actualiza según si ya existe). */
    fun saveReview() {
        val state = _uiState.value
        val comment = state.reviewComment.trim()

        if (comment.isBlank()) {
            _uiState.update { it.copy(reviewError = "El comentario no puede estar vacío") }
            return
        }
        if (comment.length > 250) {
            _uiState.update { it.copy(reviewError = "El comentario no puede superar los 250 caracteres") }
            return
        }
        if (state.reviewRate < 0.5 || state.reviewRate > 5.0) {
            _uiState.update { it.copy(reviewError = "La calificación debe estar entre 0.5 y 5.0") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingReview = true, reviewError = null) }
            try {
                val saved = if (state.review != null) {
                    reviewRepository.update(
                        id = state.review.reviewId!!,
                        rate = state.reviewRate,
                        comment = comment,
                    )
                } else {
                    reviewRepository.create(
                        bookingId = bookingId,
                        rate = state.reviewRate,
                        comment = comment,
                    )
                }
                _uiState.update {
                    it.copy(isSavingReview = false, showReviewDialog = false, review = saved)
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingReview = false,
                        reviewError = error.toUserMessage("No se pudo guardar la reseña"),
                    )
                }
            }
        }
    }

    /** Elimina la reseña existente. */
    fun deleteReview() {
        val reviewId = _uiState.value.review?.reviewId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingReview = true, errorMessage = null) }
            try {
                reviewRepository.delete(reviewId)
                _uiState.update { it.copy(isDeletingReview = false, review = null) }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isDeletingReview = false,
                        errorMessage = error.toUserMessage("No se pudo eliminar la reseña"),
                    )
                }
            }
        }
    }
}
