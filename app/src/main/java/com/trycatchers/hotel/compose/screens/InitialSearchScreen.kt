package com.trycatchers.hotel.compose.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trycatchers.hotel.compose.components.ui.DatePickerRange
import com.trycatchers.hotel.viewmodels.RoomFinderViewModel

/** Pantalla inicial para seleccionar fechas antes de abrir filtros avanzados. */
@Composable
fun InitialSearchScreen(
    onNavigateToFilters: () -> Unit,
    viewModel: RoomFinderViewModel = hiltViewModel()
) {
    val dates by viewModel.dates.collectAsState()
    val canProceed by viewModel.canSearch.collectAsState()
    val validationError by viewModel.dateValidationError.collectAsState()

    InitialSearchView(
        dates = dates,
        canProceed = canProceed,
        validationError = validationError,
        onDatesChange = viewModel::setDatesFromInitialScreen,
        onSearchClick = onNavigateToFilters
    )
}

/**
 * Vista de búsqueda inicial desacoplada para facilitar previews y tests de UI.
 */
@Composable
fun InitialSearchView(
    dates: Pair<Long?, Long?>,
    canProceed: Boolean,
    validationError: String?,
    onDatesChange: (Pair<Long, Long>) -> Unit,
    onSearchClick: () -> Unit
) {
    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF1A0A2E), Color(0xFF16213E), Color(0xFF0F3460))
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hotel Pere Maria",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Tu estancia perfecta comienza aquí",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selecciona tus fechas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3C72),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DatePickerRange(startDate = dates.first, endDate = dates.second) { selectedDates
                        ->
                        if (selectedDates.first != null && selectedDates.second != null) {
                            onDatesChange(Pair(selectedDates.first!!, selectedDates.second!!))
                        }
                    }

                    // Error message display
                    if (!validationError.isNullOrBlank()) {
                        Text(
                            text = validationError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = canProceed,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Button(
                            onClick = onSearchClick,
                            modifier =
                                Modifier.fillMaxWidth().padding(top = 24.dp).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E3C72)
                                )
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Buscar habitaciones",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
