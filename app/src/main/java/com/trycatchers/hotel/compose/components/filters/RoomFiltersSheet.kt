package com.trycatchers.hotel.compose.components.filters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trycatchers.hotel.compose.components.ui.DropDown
import com.trycatchers.hotel.viewmodels.RoomFinderViewModel.RoomSortOption

/**
 * Bottom sheet de filtros avanzados reutilizable para listados de habitaciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFiltersSheet(
    occupants: Int,
    onOccupantsChange: (Int) -> Unit,
    needsExtraBed: Boolean,
    onNeedsExtraBedChange: (Boolean) -> Unit,
    needsCrib: Boolean,
    onNeedsCribChange: (Boolean) -> Unit,
    onlyOffers: Boolean,
    onOnlyOffersChange: (Boolean) -> Unit,
    priceRange: ClosedFloatingPointRange<Float>,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    sortOption: RoomSortOption,
    onSortOptionChange: (RoomSortOption) -> Unit,
    minimumRating: Float?,
    onMinimumRatingChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "Filtros rápidos")

            DropDown(
                items = listOf(1, 2, 3, 4, 5, 6),
                item = occupants,
                onItemSelected = onOccupantsChange,
                label = "¿Cuántas personas?",
                itemToString = { "$it persona${if (it > 1) "s" else ""}" }
            )

            HorizontalDivider()

            RoomFilterToggle(
                label = "Necesito cama extra",
                checked = needsExtraBed,
                onCheckedChange = onNeedsExtraBedChange
            )
            RoomFilterToggle(
                label = "Necesito cuna",
                checked = needsCrib,
                onCheckedChange = onNeedsCribChange
            )
            RoomFilterToggle(
                label = "Solo ofertas",
                checked = onlyOffers,
                onCheckedChange = onOnlyOffersChange
            )

            HorizontalDivider()

            RoomPriceRangeSection(
                priceRange = priceRange,
                onPriceRangeChange = onPriceRangeChange,
            )

            HorizontalDivider()

            RoomSortOptionsSection(sortOption = sortOption, onSortOptionChange = onSortOptionChange)

            HorizontalDivider()

            RoomRatingSection(
                minimumRating = minimumRating,
                onMinimumRatingChange = onMinimumRatingChange,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                Text("Aplicar filtros")
            }
        }
    }
}
