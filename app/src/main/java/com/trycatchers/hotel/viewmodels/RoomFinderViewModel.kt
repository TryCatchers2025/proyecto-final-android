package com.trycatchers.hotel.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.dtos.RoomSearchFilters
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.RoomRepository
import com.trycatchers.hotel.data.repositories.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.trycatchers.hotel.utils.formatApiDate
import com.trycatchers.hotel.utils.millisToLocalDate
import com.trycatchers.hotel.utils.toUserMessage

data class RoomSearchUiState(
    val isLoading: Boolean = false,
    val rooms: List<Room> = emptyList(),
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && rooms.isEmpty()
}

private data class RoomSearchParams(
    val startDateMillis: Long,
    val endDateMillis: Long,
    val occupants: Int,
    val isVip: Boolean,
    val needsExtraBed: Boolean,
    val needsCrib: Boolean,
    val onlyOffers: Boolean,
    val priceRange: ClosedFloatingPointRange<Float>,
    val sortOption: RoomFinderViewModel.RoomSortOption,
    val onlyWithExtras: Boolean,
    val onlyWithImages: Boolean,
    val minimumRating: Float?,
)

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
        private val DEFAULT_PRICE_RANGE: ClosedFloatingPointRange<Float> = 0f..1000f
    }

    private val _dates: MutableStateFlow<Pair<Long?, Long?>> = MutableStateFlow(Pair(null, null))
    private val _occupants: MutableStateFlow<Int> = MutableStateFlow(1)
    private val _isVip: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _needsExtraBed: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _needsCrib: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _onlyOffers: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _priceRange: MutableStateFlow<ClosedFloatingPointRange<Float>> =
        MutableStateFlow(DEFAULT_PRICE_RANGE)
    private val _sortOption: MutableStateFlow<RoomSortOption> =
        MutableStateFlow(RoomSortOption.PRICE_ASC)
    private val _onlyWithExtras: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _onlyWithImages: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _minimumRating: MutableStateFlow<Float?> = MutableStateFlow(null)

    val dates: StateFlow<Pair<Long?, Long?>> = _dates
    val occupants: StateFlow<Int> = _occupants
    val isVip: StateFlow<Boolean> = _isVip
    val needsExtraBed: StateFlow<Boolean> = _needsExtraBed
    val needsCrib: StateFlow<Boolean> = _needsCrib
    val onlyOffers: StateFlow<Boolean> = _onlyOffers
    val priceRange: StateFlow<ClosedFloatingPointRange<Float>> = _priceRange
    val sortOption: StateFlow<RoomSortOption> = _sortOption
    val onlyWithExtras: StateFlow<Boolean> = _onlyWithExtras
    val onlyWithImages: StateFlow<Boolean> = _onlyWithImages
    val minimumRating: StateFlow<Float?> = _minimumRating

    private val _canSearch: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val canSearch: StateFlow<Boolean>
        get() = _canSearch

    private val _searchState = MutableStateFlow(RoomSearchUiState())
    val searchState: StateFlow<RoomSearchUiState> = _searchState.asStateFlow()

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

    fun setIsVip(isVip: Boolean) {
        _isVip.value = isVip
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

    fun setOnlyWithExtras(onlyWithExtras: Boolean) {
        _onlyWithExtras.value = onlyWithExtras
    }

    fun setOnlyWithImages(onlyWithImages: Boolean) {
        _onlyWithImages.value = onlyWithImages
    }

    fun setMinimumRating(minimumRating: Float) {
        _minimumRating.value = minimumRating.takeIf { it > 0f }
    }

    fun reset() {
        _dates.value = Pair(null, null)
        _occupants.value = 1
        _isVip.value = false
        _needsExtraBed.value = false
        _needsCrib.value = false
        _onlyOffers.value = false
        _priceRange.value = DEFAULT_PRICE_RANGE
        _sortOption.value = RoomSortOption.PRICE_ASC
        _onlyWithExtras.value = false
        _onlyWithImages.value = false
        _minimumRating.value = null
        _searchState.value = RoomSearchUiState()
        _canSearch.value = false
        lastParams = null
    }

    private fun areDatesValid(startMillis: Long?, endMillis: Long?): Boolean {
        if (startMillis == null || endMillis == null) return false
        if (startMillis >= endMillis) return false

        val startDate = millisToLocalDate(startMillis)
        val today = millisToLocalDate(System.currentTimeMillis())
        if (startDate.isBefore(today)) return false

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
                isVip = _isVip.value,
                needsExtraBed = _needsExtraBed.value,
                needsCrib = _needsCrib.value,
                onlyOffers = _onlyOffers.value,
                priceRange = _priceRange.value,
                sortOption = _sortOption.value,
                onlyWithExtras = _onlyWithExtras.value,
                onlyWithImages = _onlyWithImages.value,
                minimumRating = _minimumRating.value,
            )

        if (!forceRefresh && params == lastParams && _searchState.value.rooms.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _searchState.value = RoomSearchUiState(isLoading = true)

            try {
                val filters = params.toFilters()
                val rooms = withContext(Dispatchers.IO) { roomRepository.searchAvailable(filters) }
                lastParams = params

                val processedRooms = params.applyClientFilters(rooms)

                _searchState.value = RoomSearchUiState(rooms = processedRooms)
            } catch (error: Exception) {
                _searchState.value =
                    RoomSearchUiState(
                        errorMessage =
                            error.toUserMessage(
                                defaultMessage =
                                    "No se pudieron cargar las habitaciones disponibles."
                            )
                    )
            }
        }
    }

    private fun RoomSearchParams.applyClientFilters(source: List<Room>): List<Room> {
        var filtered = source.asSequence()

        filtered = filtered.filter { room -> room.occupancyLimit >= occupants }

        if (onlyOffers) {
            filtered = filtered.filter { (it.offerPercentage ?: 0.0) > 0.0 }
        }

        if (needsCrib) {
            filtered = filtered.filter { it.hasCradle }
        }

        if (needsExtraBed) {
            filtered = filtered.filter { it.hasExtraBed }
        }

        if (isVip) {
            filtered = filtered.filter { it.type.contains("suite", ignoreCase = true) }
        }

        filtered =
            filtered.filter { room ->
                room.pricePerNight >= priceRange.start &&
                        room.pricePerNight <= priceRange.endInclusive
            }

        if (onlyWithExtras) {
            filtered = filtered.filter { it.extras.isNotEmpty() }
        }

        if (onlyWithImages) {
            filtered = filtered.filter { !it.mainImage.isNullOrBlank() }
        }

        minimumRating?.let { threshold ->
            filtered = filtered.filter { (it.rate ?: 0.0) >= threshold.toDouble() }
        }

        val filteredList = filtered.toList()

        return when (sortOption) {
            RoomSortOption.PRICE_ASC -> filteredList.sortedBy { it.pricePerNight }
            RoomSortOption.PRICE_DESC -> filteredList.sortedByDescending { it.pricePerNight }
            RoomSortOption.RATING_DESC -> filteredList.sortedByDescending { it.rate ?: 0.0 }
        }
    }

    private fun RoomSearchParams.toFilters(): RoomSearchFilters {
        val minPrice = priceRange.start.roundToInt()
        val maxPrice = priceRange.endInclusive.roundToInt()

        val normalizedMin = minPrice.takeIf { priceRange.start > DEFAULT_PRICE_RANGE.start }
        val normalizedMax =
            maxPrice.takeIf { priceRange.endInclusive < DEFAULT_PRICE_RANGE.endInclusive }

        return RoomSearchFilters(
            startDate = formatApiDate(startDateMillis),
            endDate = formatApiDate(endDateMillis),
            occupants = occupants,
            onlyOffers = onlyOffers,
            needsCrib = needsCrib,
            needsExtraBed = needsExtraBed,
            isVip = isVip,
            minPrice = normalizedMin,
            maxPrice = normalizedMax,
        )
    }

    private fun updateCanSearch() {
        val (start, end) = _dates.value
        _canSearch.value = areDatesValid(start, end)
    }

    init {
        sessionRepository.saveSession(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2OTc3ODM3YjBiZTMyOTdlZmFiNTM1MTAiLCJyb2xlIjoiY3VzdG9tZXIiLCJpYXQiOjE3NzA3NTExMzgsImV4cCI6MTc3MjA0NzEzOH0.GgSiRq-INeSMzUMxqH7nTC8AUZe1czAqan5keN2qXZo"
        )
    }
}
