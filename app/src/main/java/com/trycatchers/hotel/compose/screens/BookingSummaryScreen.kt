package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.trycatchers.hotel.compose.components.booking.BookingDetailRow
import com.trycatchers.hotel.compose.components.booking.BookingErrorCard
import com.trycatchers.hotel.compose.components.booking.BookingScreenHeader
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.viewmodels.BookingSummaryEvent
import com.trycatchers.hotel.viewmodels.BookingSummaryUiState
import com.trycatchers.hotel.viewmodels.BookingSummaryViewModel
import kotlinx.coroutines.launch

/** Pantalla de resumen previa al pago donde se valida la reserva y se inicia su creación. */
@Composable
fun BookingSummaryScreen(
        onNavigateBack: () -> Unit,
        onNavigateToPayment: (String) -> Unit,
        viewModel: BookingSummaryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BookingSummaryEvent.NavigateToPayment -> onNavigateToPayment(event.bookingId)
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage
        if (!message.isNullOrBlank()) {
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        when {
            state.isLoading -> BookingSummaryLoading(modifier = Modifier.padding(innerPadding))
            else ->
                    BookingSummaryContent(
                            state = state,
                            onNavigateBack = onNavigateBack,
                            onRetry = viewModel::retryLoad,
                            onProceed = viewModel::proceedToPayment,
                            modifier = Modifier.padding(innerPadding)
                    )
        }
    }
}

@Composable
private fun BookingSummaryLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BookingSummaryContent(
        state: BookingSummaryUiState,
        onNavigateBack: () -> Unit,
        onRetry: () -> Unit,
        onProceed: () -> Unit,
        modifier: Modifier = Modifier,
) {
    Column(
            modifier =
                    modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SummaryHeader(onNavigateBack = onNavigateBack)

        if (state.room != null) {
            SummaryRoomCard(room = state.room)
        } else {
            val message = state.errorMessage ?: "No se pudo cargar la información de la habitación."
            BookingErrorCard(message = message, actionLabel = "Reintentar", onAction = onRetry)
        }

        SummaryBookingInfo(state = state)

        SummaryPriceCard(state = state)

        Button(
                onClick = onProceed,
                enabled = !state.isCreatingBooking && state.room != null,
                modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isCreatingBooking) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(text = "Ir a pago")
        }
    }
}

@Composable
private fun SummaryHeader(onNavigateBack: () -> Unit) {
    BookingScreenHeader(title = "Resumen de la reserva", onNavigateBack = onNavigateBack)
}

@Composable
private fun SummaryRoomCard(room: Room) {
    Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val imageUrl =
                    room.mainImage?.takeIf { it.isNotBlank() } ?: room.extraImages.firstOrNull()
            if (imageUrl != null) {
                AsyncImage(
                        model = imageUrl,
                        contentDescription = "Imagen de ${room.name}",
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentScale = ContentScale.Crop
                )
            } else {
                Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "Sin imagen disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                        text = room.name.ifBlank { "Habitación ${room.number}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = room.type, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = room.description.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SummaryBookingInfo(state: BookingSummaryUiState) {
    Card(
            shape = RoundedCornerShape(16.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Detalles de la estancia", style = MaterialTheme.typography.titleSmall)
            BookingDetailRow(label = "Entrada", value = state.startDateLabel)
            BookingDetailRow(label = "Salida", value = state.endDateLabel)
            BookingDetailRow(label = "Noches", value = state.nights.toString())
            BookingDetailRow(label = "Huéspedes", value = state.occupants.toString())
        }
    }
}

@Composable
private fun SummaryPriceCard(state: BookingSummaryUiState) {
    Card(
            shape = RoundedCornerShape(16.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Resumen de pago", style = MaterialTheme.typography.titleSmall)
            BookingDetailRow(label = "Precio por noche", value = state.formattedPricePerNight)
            BookingDetailRow(label = "Noches", value = state.nights.toString())
            HorizontalDivider()
            BookingDetailRow(label = "Total", value = state.formattedTotalPrice, emphasize = true)
        }
    }
}
