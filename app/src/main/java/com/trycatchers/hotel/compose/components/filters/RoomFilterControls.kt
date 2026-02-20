package com.trycatchers.hotel.compose.components.filters

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trycatchers.hotel.viewmodels.RoomFinderViewModel.RoomSortOption
import kotlin.math.roundToInt

/** Controles reutilizables para filtros de habitaciones. */
@Composable
fun RoomFilterToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

/** Bloque reutilizable del rango de precio por noche. */
@Composable
fun RoomPriceRangeSection(
    priceRange: ClosedFloatingPointRange<Float>,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    min: Float = 0f,
    max: Float = 500f,
) {
    Text(
        text = "Rango de precios por noche",
        style = MaterialTheme.typography.titleMedium,
    )

    RangeSlider(
        value = priceRange,
        onValueChange = onPriceRangeChange,
        valueRange = min..max,
        steps = 100,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = "${priceRange.start.toInt()}€")
        Text(text = "${priceRange.endInclusive.toInt()}€")
    }
}

/** Bloque reutilizable de ordenación de resultados. */
@Composable
fun RoomSortOptionsSection(
    sortOption: RoomSortOption,
    onSortOptionChange: (RoomSortOption) -> Unit,
) {
    Text(text = "Ordenar por")
    RoomSortOption.values().forEach { option ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = option == sortOption,
                onClick = { onSortOptionChange(option) },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = option.label)
        }
    }
}

/** Bloque reutilizable de valoración mínima. */
@Composable
fun RoomRatingSection(
    minimumRating: Float?,
    onMinimumRatingChange: (Float) -> Unit,
) {
    val sliderValue = minimumRating ?: 0f
    Text(text = "Valoración mínima")
    Slider(
        value = sliderValue,
        onValueChange = { value ->
            val normalized = if (value < 0.5f) 0f else value.roundToInt().toFloat()
            onMinimumRatingChange(normalized)
        },
        valueRange = 0f..5f,
        steps = 4,
    )

    Text(
        text = if (sliderValue <= 0f) "Cualquiera" else "${sliderValue.toInt()}+ estrellas",
    )
}

val RoomSortOption.label: String
    get() =
        when (this) {
            RoomSortOption.PRICE_ASC -> "Precio ascendente"
            RoomSortOption.PRICE_DESC -> "Precio descendente"
            RoomSortOption.RATING_DESC -> "Mejor valoración"
        }
