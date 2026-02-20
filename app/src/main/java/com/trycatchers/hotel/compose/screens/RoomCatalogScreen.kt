package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trycatchers.hotel.compose.components.filters.RoomFiltersSheet
import com.trycatchers.hotel.compose.components.rooms.RoomCard
import com.trycatchers.hotel.viewmodels.RoomCatalogFilter
import com.trycatchers.hotel.viewmodels.RoomCatalogViewModel

/**
 * Pantalla de catálogo de habitaciones.
 *
 * Muestra listado de habitaciones con chips de ordenación y acceso a filtros avanzados mediante
 * `RoomFiltersSheet`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomCatalogScreen(
    navigateToRoomDetails: (String) -> Unit,
    viewModel: RoomCatalogViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val showFilters = remember { mutableStateOf(false) }

    if (showFilters.value) {
        RoomFiltersSheet(
            occupants = state.occupants,
            onOccupantsChange = viewModel::setOccupants,
            isVip = state.isVip,
            onVipChange = viewModel::setIsVip,
            needsExtraBed = state.needsExtraBed,
            onNeedsExtraBedChange = viewModel::setNeedsExtraBed,
            needsCrib = state.needsCrib,
            onNeedsCribChange = viewModel::setNeedsCrib,
            onlyOffers = state.onlyOffers,
            onOnlyOffersChange = viewModel::setOnlyOffers,
            priceRange = state.priceRange,
            onPriceRangeChange = viewModel::setPriceRange,
            sortOption = state.sortOption,
            onSortOptionChange = viewModel::setSortOption,
            onlyWithExtras = state.onlyWithExtras,
            onOnlyWithExtrasChange = viewModel::setOnlyWithExtras,
            onlyWithImages = state.onlyWithImages,
            onOnlyWithImagesChange = viewModel::setOnlyWithImages,
            minimumRating = state.minimumRating,
            onMinimumRatingChange = viewModel::setMinimumRating,
            onDismiss = { showFilters.value = false },
            onApply = {
                viewModel.applyFilters()
                showFilters.value = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Habitaciones",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = viewModel::loadRooms) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refrescar")
            }
        }

        FilterRow(
            selected = state.selectedFilter,
            onSelect = viewModel::setFilter,
            onShowFilters = { showFilters.value = true }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.errorMessage != null ->
                    ErrorView(message = state.errorMessage!!, onRetry = viewModel::loadRooms)

                state.rooms.isEmpty() ->
                    Text(
                        "No hay habitaciones disponibles actualmente.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.rooms) { room ->
                            RoomCard(
                                room = room,
                                onViewDetails = navigateToRoomDetails,
                                onReserve =
                                    navigateToRoomDetails, // Redirect to details first if
                                // no dates
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

@Composable
private fun FilterRow(
    selected: RoomCatalogFilter,
    onSelect: (RoomCatalogFilter) -> Unit,
    onShowFilters: () -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilledTonalIconButton(onClick = onShowFilters) {
                Icon(imageVector = Icons.Rounded.Tune, contentDescription = "Filtros")
            }
        }

        items(RoomCatalogFilter.entries.toList()) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}
