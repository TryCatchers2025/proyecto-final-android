package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trycatchers.hotel.compose.components.booking.BookingDetailRow
import com.trycatchers.hotel.compose.components.booking.BookingErrorCard
import com.trycatchers.hotel.compose.components.booking.BookingScreenHeader
import com.trycatchers.hotel.viewmodels.BookingDetailEvent
import com.trycatchers.hotel.viewmodels.BookingDetailUiState
import com.trycatchers.hotel.viewmodels.BookingDetailViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de una reserva.
 *
 * Muestra la información completa de la reserva y permite:
 * - Cancelar la reserva (si está activa y no pagada)
 * - Extender la fecha de salida (si está activa)
 * - Ir a pagar (si está activa y no pagada)
 * - Crear, editar o eliminar la reseña (si la reserva ya finalizó)
 *
 * @param onNavigateBack Callback para volver a la pantalla anterior
 * @param onNavigateToPayment Callback para navegar a la pantalla de pago
 * @param viewModel ViewModel inyectado por Hilt
 */
@Composable
fun BookingDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (String) -> Unit,
    viewModel: BookingDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BookingDetailEvent.NavigateToPayment -> {
                    val bookingId = state.booking?.bookingId ?: return@collect
                    onNavigateToPayment(bookingId)
                }
                is BookingDetailEvent.BookingCanceled -> {
                    scope.launch { snackbarHostState.showSnackbar("Reserva cancelada correctamente") }
                }
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (!msg.isNullOrBlank()) scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar reserva") },
            text = { Text("¿Estás seguro de que quieres cancelar esta reserva? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelBooking()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Cancelar reserva") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Volver") }
            },
        )
    }

    // Extend date picker dialog
    if (state.showExtendDialog) {
        ExtendBookingDialog(
            currentEndDateMillis = state.extendEndDateMillis,
            error = state.extendError,
            isLoading = state.isExtending,
            onDateSelected = viewModel::setExtendEndDate,
            onConfirm = viewModel::confirmExtend,
            onDismiss = viewModel::dismissExtendDialog,
        )
    }

    // Review dialog
    if (state.showReviewDialog) {
        ReviewDialog(
            rate = state.reviewRate,
            comment = state.reviewComment,
            error = state.reviewError,
            isLoading = state.isSavingReview,
            isEditing = state.hasReview,
            onRateChange = viewModel::setReviewRate,
            onCommentChange = viewModel::setReviewComment,
            onConfirm = viewModel::saveReview,
            onDismiss = viewModel::dismissReviewDialog,
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        when {
            state.isLoading ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            state.booking == null ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BookingErrorCard(
                        message = state.errorMessage ?: "No se pudo cargar la reserva.",
                        actionLabel = "Reintentar",
                        onAction = viewModel::load,
                    )
                }
            else ->
                BookingDetailContent(
                    state = state,
                    onNavigateBack = onNavigateBack,
                    onCancel = { showCancelDialog = true },
                    onExtend = viewModel::openExtendDialog,
                    onPay = viewModel::navigateToPayment,
                    onOpenReview = viewModel::openReviewDialog,
                    onDeleteReview = viewModel::deleteReview,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}

@Composable
private fun BookingDetailContent(
    state: BookingDetailUiState,
    onNavigateBack: () -> Unit,
    onCancel: () -> Unit,
    onExtend: () -> Unit,
    onPay: () -> Unit,
    onOpenReview: () -> Unit,
    onDeleteReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val booking = state.booking ?: return

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        BookingScreenHeader(title = "Detalle de reserva", onNavigateBack = onNavigateBack)

        // Status chip
        StatusBadge(status = booking.status, isPaid = booking.isPaid)

        // Room info card
        if (state.room != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = state.room.name.ifBlank { "Habitación ${state.room.number}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = state.room.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Stay details
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Detalles de la estancia", style = MaterialTheme.typography.titleSmall)
                BookingDetailRow(label = "Check-in", value = state.checkInLabel)
                BookingDetailRow(label = "Check-out", value = state.checkOutLabel)
                BookingDetailRow(label = "Noches", value = state.nights.toString())
                BookingDetailRow(label = "Huéspedes", value = booking.occupants.toString())
                if (booking.discount > 0) {
                    BookingDetailRow(label = "Descuento", value = "${booking.discount.toInt()}%")
                }
            }
        }

        // Price summary
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Resumen de pago", style = MaterialTheme.typography.titleSmall)
                BookingDetailRow(
                    label = "Precio/noche",
                    value = "%.2f €".format(booking.pricePerNight),
                )
                HorizontalDivider()
                BookingDetailRow(
                    label = "Total",
                    value = state.formattedTotalPrice,
                    emphasize = true,
                )
            }
        }

        // Actions
        if (!state.isCanceled) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.canPay) {
                    Button(
                        onClick = onPay,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Ir a pagar") }
                }

                if (state.canExtend) {
                    OutlinedButton(
                        onClick = onExtend,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Extender estancia") }
                }

                if (state.canCancel) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                    ) {
                        if (state.isCanceling) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Cancelar reserva")
                    }
                }
            }
        }

        // Review section
        if (state.canReview || state.hasReview) {
            HorizontalDivider()
            ReviewSection(
                review = state.review,
                canReview = state.canReview,
                isDeletingReview = state.isDeletingReview,
                onOpenReview = onOpenReview,
                onDeleteReview = onDeleteReview,
            )
        }
    }
}

@Composable
private fun StatusBadge(status: String, isPaid: Boolean) {
    val (label, color) = when {
        status == "canceled" -> "Cancelada" to MaterialTheme.colorScheme.error
        isPaid -> "Pagada" to Color(0xFF2E7D32)
        else -> "Activa – pago pendiente" to MaterialTheme.colorScheme.primary
    }
    Card(
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ReviewSection(
    review: com.trycatchers.hotel.data.models.Review?,
    canReview: Boolean,
    isDeletingReview: Boolean,
    onOpenReview: () -> Unit,
    onDeleteReview: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tu reseña", style = MaterialTheme.typography.titleSmall)

        if (review != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StarRatingDisplay(rating = review.rate)
                    Text(
                        text = review.comment,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onOpenReview) { Text("Editar") }
                        TextButton(
                            onClick = onDeleteReview,
                            enabled = !isDeletingReview,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                        ) {
                            if (isDeletingReview) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        } else if (canReview) {
            Button(onClick = onOpenReview, modifier = Modifier.fillMaxWidth()) {
                Text("Escribir reseña")
            }
        } else {
            Text(
                text = "Podrás dejar una reseña cuando finalice tu estancia.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StarRatingDisplay(rating: Double) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtendBookingDialog(
    currentEndDateMillis: Long?,
    error: String?,
    isLoading: Boolean,
    onDateSelected: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentEndDateMillis)

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    ) {
        DatePicker(state = datePickerState)
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun ReviewDialog(
    rate: Double,
    comment: String,
    error: String?,
    isLoading: Boolean,
    isEditing: Boolean,
    onRateChange: (Double) -> Unit,
    onCommentChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar reseña" else "Escribir reseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Star rating selector
                Text("Calificación", style = MaterialTheme.typography.labelMedium)
                Row {
                    for (i in 1..5) {
                        IconButton(onClick = { onRateChange(i.toDouble()) }) {
                            Icon(
                                imageVector = if (i <= rate) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$i estrellas",
                                tint = Color(0xFFFFC107),
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    label = { Text("Comentario") },
                    placeholder = { Text("Cuéntanos tu experiencia…") },
                    minLines = 3,
                    maxLines = 6,
                    supportingText = { Text("${comment.length}/250") },
                    isError = !error.isNullOrBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (!error.isNullOrBlank()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (isEditing) "Guardar cambios" else "Publicar reseña")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
