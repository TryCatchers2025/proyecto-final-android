package com.trycatchers.hotel.viewmodels

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.dtos.UserDto
import com.trycatchers.hotel.data.models.Booking
import com.trycatchers.hotel.data.repositories.BookingRepository
import com.trycatchers.hotel.data.repositories.SessionRepository
import com.trycatchers.hotel.utils.parseApiDate
import com.trycatchers.hotel.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import javax.inject.Inject

/** Estado de la pantalla de cuenta de usuario. */
data class UserAccountUiState(
    val isLoading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val errorMessage: String? = null,
    val selectedFilter: BookingFilter = BookingFilter.ALL,
) {
    val filteredBookings: List<Booking>
        get() =
            when (selectedFilter) {
                BookingFilter.ALL -> bookings
                BookingFilter.ACTIVE ->
                    bookings.filter { it.status == "active" && !it.isPaid }

                BookingFilter.PAID ->
                    bookings.filter { it.isPaid }

                BookingFilter.CANCELED ->
                    bookings.filter { it.status == "canceled" }

                BookingFilter.PAST -> {
                    val today = LocalDate.now()
                    bookings.filter { b ->
                        val end = parseApiDate(b.endDate)
                        end != null && today.isAfter(end)
                    }
                }
            }

    val nextBooking: Booking?
        get() = bookings
            .filter { it.status == "active" && it.isPaid }
            .sortedBy { it.startDate }
            .lastOrNull {
                val start = parseApiDate(it.startDate)
                start != null && !start.isBefore(LocalDate.now())
            }

    val isEmpty: Boolean
        get() = filteredBookings.isEmpty()
}

/** Filtros disponibles en el historial de reservas. */
enum class BookingFilter(val label: String) {
    ALL("Todas"),
    ACTIVE("Pendientes"),
    PAID("Pagadas"),
    CANCELED("Canceladas"),
    PAST("Pasadas"),
}

/**
 * ViewModel para la pantalla de cuenta de usuario.
 *
 * Carga el historial de reservas del usuario autenticado y expone filtros por estado.
 */
@HiltViewModel
class UserAccountViewModel
@Inject
constructor(
    private val bookingRepository: BookingRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserAccountUiState())
    val uiState: StateFlow<UserAccountUiState> = _uiState.asStateFlow()
    val currentUser: StateFlow<UserDto?> = sessionRepository.currentUser

    init {
        loadBookings()
    }

    /** Carga todas las reservas del usuario autenticado. */
    fun loadBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val bookings = bookingRepository.getAll()
                    .sortedByDescending { it.bookingDate ?: it.startDate }
                _uiState.update { it.copy(isLoading = false, bookings = bookings) }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            error.toUserMessage("No se pudo cargar el historial de reservas"),
                    )
                }
            }
        }
    }

    /** Cambia el filtro activo. */
    fun setFilter(filter: BookingFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    /** Devuelve el nombre del usuario extraído del JWT, o null si no hay sesión. */
    fun getUserDisplayName(): String? {
        val token = sessionRepository.getToken() ?: return null
        val parts = token.split('.')
        if (parts.size < 2) return null
        return runCatching {
            val decoded = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
            val json = JSONObject(String(decoded, StandardCharsets.UTF_8))
            json.optString("name").takeIf { it.isNotBlank() }
                ?: json.optString("email").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
