package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trycatchers.hotel.compose.components.filters.RoomFiltersSheet
import com.trycatchers.hotel.compose.components.rooms.RoomCard
import com.trycatchers.hotel.utils.formatDateRange
import com.trycatchers.hotel.viewmodels.RoomFinderViewModel
import com.trycatchers.hotel.viewmodels.RoomSearchUiState

@Composable
fun RoomSelectorScreen(
    onNavigateBack: () -> Unit,
    navigateToRoomDetails: (String) -> Unit,
    navigateToBookingSummary:
        (
        roomId: String,
        startDateMillis: Long,
        endDateMillis: Long,
        occupants: Int,
    ) -> Unit,
    viewModel: RoomFinderViewModel = hiltViewModel()
) {
    val searchState by viewModel.searchState.collectAsState()
    val dates by viewModel.dates.collectAsState()
    val occupants by viewModel.occupants.collectAsState()
    val isVip by viewModel.isVip.collectAsState()
    val needsExtraBed by viewModel.needsExtraBed.collectAsState()
    val needsCrib by viewModel.needsCrib.collectAsState()
    val onlyOffers by viewModel.onlyOffers.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val onlyWithExtras by viewModel.onlyWithExtras.collectAsState()
    val onlyWithImages by viewModel.onlyWithImages.collectAsState()
    val minimumRating by viewModel.minimumRating.collectAsState()

    val showFilters = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.searchAvailableRooms() }

    if (showFilters.value) {
        RoomFiltersSheet(
            occupants = occupants,
            onOccupantsChange = viewModel::setOccupants,
            isVip = isVip,
            onVipChange = viewModel::setIsVip,
            needsExtraBed = needsExtraBed,
            onNeedsExtraBedChange = viewModel::setNeedsExtraBed,
            needsCrib = needsCrib,
            onNeedsCribChange = viewModel::setNeedsCrib,
            onlyOffers = onlyOffers,
            onOnlyOffersChange = viewModel::setOnlyOffers,
            priceRange = priceRange,
            onPriceRangeChange = viewModel::setPriceRange,
            sortOption = sortOption,
            onSortOptionChange = viewModel::setSortOption,
            onlyWithExtras = onlyWithExtras,
            onOnlyWithExtrasChange = viewModel::setOnlyWithExtras,
            onlyWithImages = onlyWithImages,
            onOnlyWithImagesChange = viewModel::setOnlyWithImages,
            minimumRating = minimumRating,
            onMinimumRatingChange = viewModel::setMinimumRating,
            onDismiss = { showFilters.value = false },
            onApply = {
                viewModel.searchAvailableRooms(forceRefresh = true)
                showFilters.value = false
            }
        )
    }

    RoomSelectorView(
        state = searchState,
        dates = dates,
        occupants = occupants,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.searchAvailableRooms(forceRefresh = true) },
        onViewDetails = navigateToRoomDetails,
        onReserve = { roomId ->
            val start = dates.first
            val end = dates.second
            if (start != null && end != null) {
                navigateToBookingSummary(roomId, start, end, occupants)
            }
        },
        onShowFilters = { showFilters.value = true }
    )
}

@Composable
fun RoomSelectorView(
    state: RoomSearchUiState,
    dates: Pair<Long?, Long?>,
    occupants: Int,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onViewDetails: (String) -> Unit,
    onReserve: (String) -> Unit,
    onShowFilters: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SelectorHeader(
            onNavigateBack = onNavigateBack,
            onShowFilters = onShowFilters,
            dates = dates,
            occupants = occupants
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            state.isLoading -> SelectorLoading()
            state.errorMessage != null ->
                SelectorError(message = state.errorMessage, onRetry = onRetry)

            state.isEmpty -> SelectorEmpty()
            else ->
                SelectorList(
                    state = state,
                    onViewDetails = onViewDetails,
                    onReserve = onReserve
                )
        }
    }
}

@Composable
private fun SelectorHeader(
    onNavigateBack: () -> Unit,
    onShowFilters: () -> Unit,
    dates: Pair<Long?, Long?>,
    occupants: Int,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            Text(
                text = "Habitaciones disponibles",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onShowFilters) {
                Icon(imageVector = Icons.Rounded.Tune, contentDescription = "Filtros")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val dateRangeLabel =
            formatDateRange(dates.first, dates.second) ?: "Selecciona fechas validas"

        Text(
            text = dateRangeLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Huéspedes: $occupants",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectorLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SelectorError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

@Composable
private fun SelectorEmpty() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No se encontraron habitaciones para las fechas seleccionadas.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SelectorList(
    state: RoomSearchUiState,
    onViewDetails: (String) -> Unit,
    onReserve: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.rooms) { room ->
            RoomCard(
                room = room,
                onViewDetails = onViewDetails,
                onReserve = onReserve,
                modifier = Modifier.fillMaxWidth()
            ) { modifier ->
                Button(
                    onClick = { onReserve(room.id) },
                    modifier = modifier
                ) {
                    Text("Reservar")
                }
            }
        }
    }
}
