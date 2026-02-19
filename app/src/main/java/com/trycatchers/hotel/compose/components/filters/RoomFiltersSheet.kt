package com.trycatchers.hotel.compose.components.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trycatchers.hotel.compose.components.ui.DropDown
import com.trycatchers.hotel.viewmodels.RoomFinderViewModel.RoomSortOption
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFiltersSheet(
        occupants: Int,
        onOccupantsChange: (Int) -> Unit,
        isVip: Boolean,
        onVipChange: (Boolean) -> Unit,
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
        onlyWithExtras: Boolean,
        onOnlyWithExtrasChange: (Boolean) -> Unit,
        onlyWithImages: Boolean,
        onOnlyWithImagesChange: (Boolean) -> Unit,
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

            FilterToggle(label = "Cliente VIP", checked = isVip, onCheckedChange = onVipChange)
            FilterToggle(
                    label = "Necesito cama extra",
                    checked = needsExtraBed,
                    onCheckedChange = onNeedsExtraBedChange
            )
            FilterToggle(
                    label = "Necesito cuna",
                    checked = needsCrib,
                    onCheckedChange = onNeedsCribChange
            )
            FilterToggle(
                    label = "Solo ofertas",
                    checked = onlyOffers,
                    onCheckedChange = onOnlyOffersChange
            )

            HorizontalDivider()

            Text(text = "Rango de precios")
            RangeSlider(
                    value = priceRange,
                    onValueChange = onPriceRangeChange,
                    valueRange = 0f..1000f,
                    steps = 4
            )
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${priceRange.start.toInt()}€")
                Text(text = "${priceRange.endInclusive.toInt()}€")
            }

            HorizontalDivider()

            Text(text = "Ordenar por")
            RoomSortOption.values().forEach { option ->
                SortOptionRow(
                        label = option.label,
                        selected = option == sortOption,
                        onSelected = { onSortOptionChange(option) }
                )
            }

            HorizontalDivider()

            Text(text = "Filtros adicionales")
            FilterToggle(
                    label = "Solo con extras incluidos",
                    checked = onlyWithExtras,
                    onCheckedChange = onOnlyWithExtrasChange
            )
            FilterToggle(
                    label = "Solo con imagen",
                    checked = onlyWithImages,
                    onCheckedChange = onOnlyWithImagesChange
            )

            val sliderValue = minimumRating ?: 0f
            Text(text = "Valoración mínima")
            Slider(
                    value = sliderValue,
                    onValueChange = { value ->
                        val normalized = if (value < 0.5f) 0f else value.roundToInt().toFloat()
                        onMinimumRatingChange(normalized)
                    },
                    valueRange = 0f..5f,
                    steps = 4
            )
            Text(
                    text =
                            if (sliderValue <= 0f) "Cualquiera"
                            else "${sliderValue.toInt()}+ estrellas"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                Text("Aplicar filtros")
            }
        }
    }
}

@Composable
private fun FilterToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

@Composable
private fun SortOptionRow(label: String, selected: Boolean, onSelected: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onSelected)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

private val RoomSortOption.label: String
    get() =
            when (this) {
                RoomSortOption.PRICE_ASC -> "Precio ascendente"
                RoomSortOption.PRICE_DESC -> "Precio descendente"
                RoomSortOption.RATING_DESC -> "Mejor valoración"
            }
