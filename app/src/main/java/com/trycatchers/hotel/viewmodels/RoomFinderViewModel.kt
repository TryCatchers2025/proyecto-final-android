package com.trycatchers.hotel.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.RoomRepository
import com.trycatchers.hotel.data.repositories.SessionRepository
import com.trycatchers.hotel.utils.millisToLocalDate
import com.trycatchers.hotel.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class RoomSearchUiState(
    val isLoading: Boolean = false,
    val rooms: List<Room> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && rooms.isEmpty()
}

/** Estado agregado de filtros para minimizar acoplamiento entre vistas y flujos internos. */
data class RoomFinderFiltersUiState(
    val dates: Pair<Long?, Long?> = Pair(null, null),
    val occupants: Int = 1,
    val needsExtraBed: Boolean = false,
    val needsCrib: Boolean = false,
    val onlyOffers: Boolean = false,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..500f,
    val sortOption: RoomFinderViewModel.RoomSortOption = RoomFinderViewModel.RoomSortOption.PRICE_ASC,
    val minimumRating: Float? = null,
    val canSearch: Boolean = false,
    val dateValidationError: String? = null,
)

private data class RoomSearchParams(
    val startDateMillis: Long,
    val endDateMillis: Long,
    val occupants: Int,
    val needsExtraBed: Boolean,
    val needsCrib: Boolean,
    val onlyOffers: Boolean,
    val priceRange: ClosedFloatingPointRange<Float>,
    val sortOption: RoomFinderViewModel.RoomSortOption,
    val minimumRating: Float?,
)

/**
 * ViewModel del flujo de búsqueda de habitaciones con estado de filtros y resultados.
 */
@HiltViewModel
class RoomFinderViewModel
@Inject
constructor(
    private val sessionRepository: SessionRepository,
    private val roomRepository: RoomRepository,
) : ViewModel() {

    enum class RoomSortOption {
        PRICE_ASC,
        PRICE_DESC,
        RATING_DESC
    }

    companion object {
        private val DEFAULT_PRICE_RANGE: ClosedFloatingPointRange<Float> = 0f..500f
    }

    private val _dates: MutableStateFlow<Pair<Long?, Long?>> = MutableStateFlow(Pair(null, null))
    private val _occupants: MutableStateFlow<Int> = MutableStateFlow(1)
    private val _needsExtraBed: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _needsCrib: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _onlyOffers: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _priceRange: MutableStateFlow<ClosedFloatingPointRange<Float>> =
        MutableStateFlow(DEFAULT_PRICE_RANGE)
    private val _sortOption: MutableStateFlow<RoomSortOption> =
        MutableStateFlow(RoomSortOption.PRICE_ASC)
    private val _minimumRating: MutableStateFlow<Float?> = MutableStateFlow(null)
    private val _selectedRoomId: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _dateValidationError: MutableStateFlow<String?> = MutableStateFlow(null)

    val dates: StateFlow<Pair<Long?, Long?>> = _dates
    val occupants: StateFlow<Int> = _occupants
    val needsExtraBed: StateFlow<Boolean> = _needsExtraBed
    val needsCrib: StateFlow<Boolean> = _needsCrib
    val onlyOffers: StateFlow<Boolean> = _onlyOffers
    val priceRange: StateFlow<ClosedFloatingPointRange<Float>> = _priceRange
    val sortOption: StateFlow<RoomSortOption> = _sortOption
    val minimumRating: StateFlow<Float?> = _minimumRating
    val selectedRoomId: StateFlow<String?> = _selectedRoomId.asStateFlow()
    val dateValidationError: StateFlow<String?> = _dateValidationError.asStateFlow()

    private val _canSearch: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val canSearch: StateFlow<Boolean>
        get() = _canSearch

    private val _searchState = MutableStateFlow(RoomSearchUiState())
    val searchState: StateFlow<RoomSearchUiState> = _searchState.asStateFlow()

    private data class PrimaryFilters(
        val dates: Pair<Long?, Long?>,
        val occupants: Int,
        val needsExtraBed: Boolean,
        val needsCrib: Boolean,
    )

    private data class SecondaryFilters(
        val onlyOffers: Boolean,
        val priceRange: ClosedFloatingPointRange<Float>,
        val sortOption: RoomSortOption,
    )

    private data class ValidationFilters(
        val minimumRating: Float?,
        val canSearch: Boolean,
        val dateValidationError: String?,
    )

    private val primaryFiltersFlow =
        combine(dates, occupants, needsExtraBed, needsCrib) {
                selectedDates,
                selectedOccupants,
                selectedNeedsExtraBed,
                selectedNeedsCrib,
            ->
            PrimaryFilters(
                dates = selectedDates,
                occupants = selectedOccupants,
                needsExtraBed = selectedNeedsExtraBed,
                needsCrib = selectedNeedsCrib,
            )
        }

    private val secondaryFiltersFlow =
        combine(onlyOffers, priceRange, sortOption) { selectedOnlyOffers, selectedPriceRange, selectedSortOption ->
            SecondaryFilters(
                onlyOffers = selectedOnlyOffers,
                priceRange = selectedPriceRange,
                sortOption = selectedSortOption
            )
        }

    private val validationFiltersFlow =
        combine(minimumRating, canSearch, dateValidationError) {
                selectedMinimumRating,
                selectedCanSearch,
                selectedDateValidationError,
            ->
            ValidationFilters(
                minimumRating = selectedMinimumRating,
                canSearch = selectedCanSearch,
                dateValidationError = selectedDateValidationError,
            )
        }

    val filtersUiState: StateFlow<RoomFinderFiltersUiState> =
        combine(primaryFiltersFlow, secondaryFiltersFlow, validationFiltersFlow) {
                primary,
                secondary,
                validation,
            ->
            RoomFinderFiltersUiState(
                dates = primary.dates,
                occupants = primary.occupants,
                needsExtraBed = primary.needsExtraBed,
                needsCrib = primary.needsCrib,
                onlyOffers = secondary.onlyOffers,
                priceRange = secondary.priceRange,
                sortOption = secondary.sortOption,
                minimumRating = validation.minimumRating,
                canSearch = validation.canSearch,
                dateValidationError = validation.dateValidationError,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RoomFinderFiltersUiState(),
        )

    private var lastParams: RoomSearchParams? = null

    fun setDates(dates: Pair<Long, Long>) {
        _dates.value = dates
        updateCanSearch()
    }

    fun setDatesFromInitialScreen(dates: Pair<Long, Long>) {
        setDates(dates)
    }

    fun setOccupants(occupants: Int) {
        _occupants.value = occupants
    }

    fun setNeedsExtraBed(needsExtraBed: Boolean) {
        _needsExtraBed.value = needsExtraBed
    }

    fun setNeedsCrib(needsCrib: Boolean) {
        _needsCrib.value = needsCrib
    }

    fun setOnlyOffers(onlyOffers: Boolean) {
        _onlyOffers.value = onlyOffers
    }

    fun setPriceRange(priceRange: ClosedFloatingPointRange<Float>) {
        _priceRange.value = priceRange
    }

    fun setSortOption(option: RoomSortOption) {
        _sortOption.value = option
    }

    fun setMinimumRating(minimumRating: Float) {
        _minimumRating.value = minimumRating.takeIf { it > 0f }
    }

    fun setSelectedRoomId(roomId: String?) {
        _selectedRoomId.value = roomId
    }

    fun reset() {
        val savedDates = _dates.value
        val savedRoomId = _selectedRoomId.value

        _dates.value = savedDates
        _occupants.value = 1
        _needsExtraBed.value = false
        _needsCrib.value = false
        _onlyOffers.value = false
        _priceRange.value = DEFAULT_PRICE_RANGE
        _sortOption.value = RoomSortOption.PRICE_ASC
        _minimumRating.value = null
        _searchState.value = RoomSearchUiState()
        _canSearch.value = false
        lastParams = null
        _selectedRoomId.value = savedRoomId
    }

    fun fullReset() {
        _dates.value = Pair(null, null)
        _occupants.value = 1
        _needsExtraBed.value = false
        _needsCrib.value = false
        _onlyOffers.value = false
        _priceRange.value = DEFAULT_PRICE_RANGE
        _sortOption.value = RoomSortOption.PRICE_ASC
        _minimumRating.value = null
        _searchState.value = RoomSearchUiState()
        _canSearch.value = false
        lastParams = null
        _selectedRoomId.value = null
    }

    private fun areDatesValid(startMillis: Long?, endMillis: Long?): Boolean {
        if (startMillis == null || endMillis == null) {
            _dateValidationError.value = "Selecciona ambas fechas"
            return false
        }
        if (startMillis >= endMillis) {
            _dateValidationError.value = "La fecha de inicio debe ser anterior a la fecha de fin"
            return false
        }

        val startDate = millisToLocalDate(startMillis)
        val today = millisToLocalDate(System.currentTimeMillis())
        if (startDate.isBefore(today)) {
            _dateValidationError.value = "La fecha de inicio no puede ser en el pasado"
            return false
        }

        _dateValidationError.value = null
        return true
    }

    fun canProceedFromInitial(): Boolean = areDatesValid(_dates.value.first, _dates.value.second)

    fun searchAvailableRooms(forceRefresh: Boolean = false) {
        val startMillis = _dates.value.first ?: return
        val endMillis = _dates.value.second ?: return

        if (_searchState.value.isLoading && !forceRefresh) {
            return
        }

        val params =
            RoomSearchParams(
                startDateMillis = startMillis,
                endDateMillis = endMillis,
                occupants = _occupants.value,
                needsExtraBed = _needsExtraBed.value,
                needsCrib = _needsCrib.value,
                onlyOffers = _onlyOffers.value,
                priceRange = _priceRange.value,
                sortOption = _sortOption.value,
                minimumRating = _minimumRating.value,
            )

        if (!forceRefresh && params == lastParams && _searchState.value.rooms.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _searchState.value = RoomSearchUiState(isLoading = true)
            try {
                val filters = com.trycatchers.hotel.data.dtos.RoomSearchFilters(
                    startDate = params.startDateMillis.toString(),
                    endDate = params.endDateMillis.toString(),
                    occupants = params.occupants,
                    onlyOffers = params.onlyOffers,
                    needsCrib = params.needsCrib,
                    needsExtraBed = params.needsExtraBed,
                    minPrice = params.priceRange.start.toInt(),
                    maxPrice = params.priceRange.endInclusive.toInt(),
                    minimumRating = params.minimumRating
                )
                val rooms = withContext(Dispatchers.IO) { roomRepository.searchAvailable(filters) }
                lastParams = params
                val sortedRooms = when (params.sortOption) {
                    RoomSortOption.PRICE_ASC -> rooms.sortedBy { it.pricePerNight }
                    RoomSortOption.PRICE_DESC -> rooms.sortedByDescending { it.pricePerNight }
                    RoomSortOption.RATING_DESC -> rooms.sortedByDescending { it.rate ?: 0.0 }
                }
                _searchState.value = RoomSearchUiState(rooms = sortedRooms)
            } catch (error: Exception) {
                _searchState.value = RoomSearchUiState(
                    errorMessage = error.toUserMessage(
                        defaultMessage = "No se pudieron cargar las habitaciones disponibles."
                    )
                )
            }
        }
    }

    private fun updateCanSearch() {
        val (start, end) = _dates.value
        _canSearch.value = areDatesValid(start, end)
    }
}
