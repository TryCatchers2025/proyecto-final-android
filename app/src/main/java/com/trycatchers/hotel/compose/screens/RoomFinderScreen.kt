package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trycatchers.hotel.compose.components.ui.DatePickerRange
import com.trycatchers.hotel.compose.components.ui.DropDown
import com.trycatchers.hotel.viewmodels.RoomFinderViewModel

/** Pantalla de filtros completos para buscar habitaciones disponibles. */
@Composable
fun RoomFinderScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSelector: () -> Unit,
    viewModel: RoomFinderViewModel = hiltViewModel()
) {
    val canSearch by viewModel.canSearch.collectAsState()
    val dates by viewModel.dates.collectAsState()
    val occupants by viewModel.occupants.collectAsState()
    val isVip by viewModel.isVip.collectAsState()
    val needsExtraBed by viewModel.needsExtraBed.collectAsState()
    val needsCrib by viewModel.needsCrib.collectAsState()
    val onlyOffers by viewModel.onlyOffers.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()

    RoomFinderView(
        onDatesChange = viewModel::setDates,
        onOccupantsChange = viewModel::setOccupants,
        onIsVipChange = viewModel::setIsVip,
        onNeedsExtraBedChange = viewModel::setNeedsExtraBed,
        onNeedsCribChange = viewModel::setNeedsCrib,
        onOnlyOffersChange = viewModel::setOnlyOffers,
        onPriceRangeChange = viewModel::setPriceRange,
        onGoBack = onNavigateBack,
        onSearch = {
            viewModel.searchAvailableRooms(forceRefresh = true)
            onNavigateToSelector()
        },
        canSearch = canSearch,
        dates = dates,
        occupants = occupants,
        isVip = isVip,
        needsExtraBed = needsExtraBed,
        needsCrib = needsCrib,
        onlyOffers = onlyOffers,
        priceRange = priceRange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/** Vista de filtros de búsqueda desacoplada del ViewModel. */
fun RoomFinderView(
    onDatesChange: (Pair<Long, Long>) -> Unit,
    onOccupantsChange: (Int) -> Unit,
    onIsVipChange: (Boolean) -> Unit,
    onNeedsExtraBedChange: (Boolean) -> Unit,
    onNeedsCribChange: (Boolean) -> Unit,
    onOnlyOffersChange: (Boolean) -> Unit,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onGoBack: () -> Unit,
    onSearch: () -> Unit,
    canSearch: Boolean,
    dates: Pair<Long?, Long?>,
    occupants: Int,
    isVip: Boolean,
    needsExtraBed: Boolean,
    needsCrib: Boolean,
    onlyOffers: Boolean,
    priceRange: ClosedFloatingPointRange<Float>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onGoBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            Text(
                text = "Filtros de búsqueda",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DatePickerRange(
                startDate = dates.first,
                endDate = dates.second,
            ) {
                if (it.first != null && it.second != null) {
                    onDatesChange(Pair(it.first!!, it.second!!))
                }
            }

            HorizontalDivider()

            DropDown(
                items = listOf(1, 2, 3, 4, 5, 6),
                item = occupants,
                onItemSelected = onOccupantsChange,
                label = "¿Cuántas personas?",
                itemToString = { "$it persona${if (it > 1) "s" else ""}" },
            )

            HorizontalDivider()

            Text(
                text = "Opciones adicionales",
                style = MaterialTheme.typography.titleMedium,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isVip,
                    onCheckedChange = onIsVipChange,
                )
                Text(text = "¿Eres VIP?")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = needsExtraBed,
                    onCheckedChange = onNeedsExtraBedChange,
                )
                Text(text = "Necesito una cama extra")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = needsCrib,
                    onCheckedChange = onNeedsCribChange,
                )
                Text(text = "Necesito una cuna")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = onlyOffers,
                    onCheckedChange = onOnlyOffersChange,
                )
                Text(text = "Solo ver ofertas")
            }

            HorizontalDivider()

            Text(
                text = "Rango de precios por noche",
                style = MaterialTheme.typography.titleMedium,
            )

            RangeSlider(
                value = priceRange,
                onValueChange = onPriceRangeChange,
                valueRange = 0f..500f,
                steps = 100,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            ) {
                Text("${priceRange.start.toInt()}€")
                Text("${priceRange.endInclusive.toInt()}€")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
        ) {
            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(),
                enabled = canSearch,
            ) { Text("Ver habitaciones disponibles") }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun RoomFinderPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        RoomFinderView(
            onDatesChange = { _ -> },
            onOccupantsChange = {},
            onIsVipChange = {},
            onNeedsExtraBedChange = {},
            onNeedsCribChange = {},
            onOnlyOffersChange = {},
            onPriceRangeChange = {},
            onGoBack = {},
            onSearch = {},
            canSearch = true,
            dates = Pair(null, null),
            occupants = 1,
            isVip = false,
            needsExtraBed = false,
            needsCrib = false,
            onlyOffers = false,
            priceRange = 0f..1000f,
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun RoomFinderFiltersPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        RoomFinderView(
            onDatesChange = { _ -> },
            onOccupantsChange = {},
            onIsVipChange = {},
            onNeedsExtraBedChange = {},
            onNeedsCribChange = {},
            onOnlyOffersChange = {},
            onPriceRangeChange = {},
            onGoBack = {},
            onSearch = {},
            canSearch = true,
            dates = Pair(1771296600904, null),
            occupants = 2,
            isVip = true,
            needsExtraBed = true,
            needsCrib = false,
            onlyOffers = true,
            priceRange = 100f..500f,
        )
    }
}
