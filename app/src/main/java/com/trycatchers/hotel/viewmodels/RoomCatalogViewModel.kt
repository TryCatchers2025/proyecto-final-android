package com.trycatchers.hotel.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.RoomRepository
import com.trycatchers.hotel.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

/** Filtros rápidos de ordenación visibles en la fila superior del catálogo. */
enum class RoomCatalogFilter(val label: String) {
    ALL("Todos"),
    PRICE_LOW("Económicos"),
    PRICE_HIGH("Lujo"),
    HIGH_RATING("Mejor valoradas")
}

/**
 * Estado UI del catálogo de habitaciones.
 *
 * Incluye datos de listado, ordenación rápida y filtros avanzados para el `RoomFiltersSheet`.
 */
data class RoomCatalogUiState(
    val isLoading: Boolean = true,
    val allRooms: List<Room> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val selectedFilter: RoomCatalogFilter = RoomCatalogFilter.ALL,
    val occupants: Int = 1,
    val isVip: Boolean = false,
    val needsExtraBed: Boolean = false,
    val needsCrib: Boolean = false,
    val onlyOffers: Boolean = false,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    val sortOption: RoomFinderViewModel.RoomSortOption = RoomFinderViewModel.RoomSortOption.PRICE_ASC,
    val onlyWithExtras: Boolean = false,
    val onlyWithImages: Boolean = false,
    val minimumRating: Float? = null,
    val errorMessage: String? = null,
)

/**
 * ViewModel del catálogo de habitaciones.
 *
 * Centraliza la carga remota y la aplicación de filtros/ordenación para mantener separación
 * estricta entre estado y vista.
 */
@HiltViewModel
class RoomCatalogViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomCatalogUiState())
    val uiState: StateFlow<RoomCatalogUiState> = _uiState.asStateFlow()

    init {
        loadRooms()
    }

    /** Carga las habitaciones y aplica el conjunto de filtros actual. */
    fun loadRooms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rooms = roomRepository.getAll()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allRooms = rooms,
                        rooms = applyFilters(rooms, it),
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.toUserMessage("No se pudo cargar el catálogo de habitaciones")
                    )
                }
            }
        }
    }

    /** Selecciona ordenación rápida desde chips y vuelve a filtrar el listado. */
    fun setFilter(filter: RoomCatalogFilter) {
        _uiState.update {
            val sortOption = mapQuickFilterToSortOption(filter, it.sortOption)
            it.copy(
                selectedFilter = filter,
                sortOption = sortOption,
                rooms = applyFilters(it.allRooms, it.copy(selectedFilter = filter, sortOption = sortOption))
            )
        }
    }

    /** Actualiza ocupantes requeridos para los filtros. */
    fun setOccupants(value: Int) = updateAndFilter { it.copy(occupants = value) }

    /** Actualiza filtro VIP. */
    fun setIsVip(value: Boolean) = updateAndFilter { it.copy(isVip = value) }

    /** Actualiza filtro de cama extra. */
    fun setNeedsExtraBed(value: Boolean) = updateAndFilter { it.copy(needsExtraBed = value) }

    /** Actualiza filtro de cuna. */
    fun setNeedsCrib(value: Boolean) = updateAndFilter { it.copy(needsCrib = value) }

    /** Actualiza filtro de ofertas. */
    fun setOnlyOffers(value: Boolean) = updateAndFilter { it.copy(onlyOffers = value) }

    /** Actualiza rango de precios. */
    fun setPriceRange(value: ClosedFloatingPointRange<Float>) =
        updateAndFilter { it.copy(priceRange = value) }

    /** Actualiza ordenación del sheet y sincroniza chips rápidos. */
    fun setSortOption(value: RoomFinderViewModel.RoomSortOption) {
        updateAndFilter {
            it.copy(
                sortOption = value,
                selectedFilter = mapSortOptionToQuickFilter(value),
            )
        }
    }

    /** Actualiza filtro de extras incluidos. */
    fun setOnlyWithExtras(value: Boolean) = updateAndFilter { it.copy(onlyWithExtras = value) }

    /** Actualiza filtro de habitaciones con imagen. */
    fun setOnlyWithImages(value: Boolean) = updateAndFilter { it.copy(onlyWithImages = value) }

    /** Actualiza filtro de valoración mínima. */
    fun setMinimumRating(value: Float) =
        updateAndFilter { it.copy(minimumRating = value.takeIf { rating -> rating > 0f }) }

    /** Fuerza reaplicación de filtros actuales. */
    fun applyFilters() {
        _uiState.update { state -> state.copy(rooms = applyFilters(state.allRooms, state)) }
    }

    private fun updateAndFilter(transform: (RoomCatalogUiState) -> RoomCatalogUiState) {
        _uiState.update { state ->
            val next = transform(state)
            next.copy(rooms = applyFilters(next.allRooms, next))
        }
    }

    private fun applyFilters(rooms: List<Room>, state: RoomCatalogUiState): List<Room> {
        val minPrice = state.priceRange.start.roundToInt().toDouble()
        val maxPrice = state.priceRange.endInclusive.roundToInt().toDouble()

        val filtered =
            rooms.asSequence()
                .filter { room -> room.occupancyLimit >= state.occupants }
                .filter { room -> !state.needsExtraBed || room.hasExtraBed }
                .filter { room -> !state.needsCrib || room.hasCradle }
                .filter { room -> !state.onlyOffers || (room.offerPercentage ?: 0.0) > 0.0 }
                .filter { room -> !state.onlyWithExtras || room.extras.isNotEmpty() }
                .filter { room -> !state.onlyWithImages || !room.mainImage.isNullOrBlank() || room.extraImages.isNotEmpty() }
                .filter { room -> state.minimumRating == null || (room.rate ?: 0.0) >= state.minimumRating }
                .filter { room ->
                    val finalPrice = room.finalPricePerNight()
                    finalPrice in minPrice..maxPrice
                }
                .toList()

        return when (state.sortOption) {
            RoomFinderViewModel.RoomSortOption.PRICE_ASC ->
                filtered.sortedBy { it.finalPricePerNight() }

            RoomFinderViewModel.RoomSortOption.PRICE_DESC ->
                filtered.sortedByDescending { it.finalPricePerNight() }

            RoomFinderViewModel.RoomSortOption.RATING_DESC ->
                filtered.sortedByDescending { it.rate ?: 0.0 }
        }
    }

    private fun mapQuickFilterToSortOption(
        filter: RoomCatalogFilter,
        fallback: RoomFinderViewModel.RoomSortOption,
    ): RoomFinderViewModel.RoomSortOption =
        when (filter) {
            RoomCatalogFilter.ALL -> fallback
            RoomCatalogFilter.PRICE_LOW -> RoomFinderViewModel.RoomSortOption.PRICE_ASC
            RoomCatalogFilter.PRICE_HIGH -> RoomFinderViewModel.RoomSortOption.PRICE_DESC
            RoomCatalogFilter.HIGH_RATING -> RoomFinderViewModel.RoomSortOption.RATING_DESC
        }

    private fun mapSortOptionToQuickFilter(sortOption: RoomFinderViewModel.RoomSortOption): RoomCatalogFilter =
        when (sortOption) {
            RoomFinderViewModel.RoomSortOption.PRICE_ASC -> RoomCatalogFilter.PRICE_LOW
            RoomFinderViewModel.RoomSortOption.PRICE_DESC -> RoomCatalogFilter.PRICE_HIGH
            RoomFinderViewModel.RoomSortOption.RATING_DESC -> RoomCatalogFilter.HIGH_RATING
        }

    private fun Room.finalPricePerNight(): Double {
        val offer = offerPercentage ?: 0.0
        return pricePerNight * (1 - offer / 100)
    }
}
