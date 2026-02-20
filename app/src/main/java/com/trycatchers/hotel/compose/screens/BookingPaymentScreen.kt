package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trycatchers.hotel.compose.components.booking.BookingDetailRow
import com.trycatchers.hotel.compose.components.booking.BookingErrorCard
import com.trycatchers.hotel.compose.components.booking.BookingScreenHeader
import com.trycatchers.hotel.viewmodels.BookingPaymentEvent
import com.trycatchers.hotel.viewmodels.BookingPaymentUiState
import com.trycatchers.hotel.viewmodels.BookingPaymentViewModel
import kotlinx.coroutines.launch

/** Pantalla de pago de reserva con resumen, simulación de confirmación y resultado final. */
@Composable
fun BookingPaymentScreen(
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit,
    viewModel: BookingPaymentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showPaymentDialog by rememberSaveable { mutableStateOf(false) }
    var showSuccessDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                BookingPaymentEvent.PaymentCompleted -> showSuccessDialog = true
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage
        if (!message.isNullOrBlank()) {
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    if (showPaymentDialog) {
        SimulatedPaymentDialog(
            amount = state.formattedTotalPrice,
            onDismiss = { showPaymentDialog = false },
            onConfirm = {
                showPaymentDialog = false
                viewModel.confirmPayment()
            }
        )
    }

    if (showSuccessDialog) {
        PaymentSuccessDialog(
            onDismiss = {
                showSuccessDialog = false
                viewModel.reset()
                onFinish()
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        when {
            state.isLoading -> BookingPaymentLoading(modifier = Modifier.padding(innerPadding))
            else ->
                BookingPaymentContent(
                    state = state,
                    onNavigateBack = onNavigateBack,
                    onReload = viewModel::loadPaymentSummary,
                    onPay = {
                        if (!state.isPaid && !state.isPaying) showPaymentDialog = true
                    },
                    modifier = Modifier.padding(innerPadding)
                )
        }
    }
}

@Composable
private fun BookingPaymentLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BookingPaymentContent(
    state: BookingPaymentUiState,
    onNavigateBack: () -> Unit,
    onReload: () -> Unit,
    onPay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BookingScreenHeader(title = "Pago de la reserva", onNavigateBack = onNavigateBack)

        if (state.room != null) {
            BookingPaymentRoomCard(state)
        } else {
            val message =
                state.errorMessage ?: "No pudimos cargar la habitación asociada a esta reserva."
            BookingErrorCard(message = message, actionLabel = "Reintentar", onAction = onReload)
        }

        BookingPaymentSummary(state)

        Button(
            onClick = onPay,
            enabled = !state.isPaid && !state.isPaying,
            modifier = Modifier.fillMaxWidth()
        ) {
            when {
                state.isPaying -> {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Procesando pago...")
                }

                state.isPaid -> Text(text = "Reserva pagada")
                else -> Text(text = "Confirmar pago")
            }
        }

        Text(
            text = "Estado: ${state.paymentStatusLabel}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BookingPaymentHeader(onNavigateBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onNavigateBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
        }
        Text(
            text = "Pago de la reserva",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun BookingPaymentRoomCard(state: BookingPaymentUiState) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = state.room?.name?.ifBlank { "Habitación" } ?: "Habitación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.room?.type.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            BookingDetailRow(label = "Check-in", value = state.checkInLabel)
            BookingDetailRow(label = "Check-out", value = state.checkOutLabel)
            BookingDetailRow(label = "Noches", value = state.nights.toString())
            BookingDetailRow(label = "Huéspedes", value = state.occupants.toString())
        }
    }
}

@Composable
private fun BookingPaymentSummary(state: BookingPaymentUiState) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Resumen de pago",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            BookingDetailRow(label = "Importe total", value = state.formattedTotalPrice)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text(
                text = "Se procesará el pago utilizando una pasarela simulada.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SimulatedPaymentDialog(
    amount: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val cardNumber = rememberSaveable { mutableStateOf("**** **** **** 4242") }
    val holderName = rememberSaveable { mutableStateOf("Nombre Apellido") }
    val cvv = rememberSaveable { mutableStateOf("123") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Simular pago") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Introduce datos ficticios para completar el pago de $amount.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = cardNumber.value,
                    onValueChange = { cardNumber.value = it },
                    label = { Text(text = "Número de tarjeta") }
                )
                OutlinedTextField(
                    value = holderName.value,
                    onValueChange = { holderName.value = it },
                    label = { Text(text = "Titular") }
                )
                OutlinedTextField(
                    value = cvv.value,
                    onValueChange = { cvv.value = it },
                    label = { Text(text = "CVV") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = "Pagar ahora") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancelar") } }
    )
}

@Composable
private fun PaymentSuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Pago completado") },
        text = {
            Text(
                text =
                    "Tu pago se procesó correctamente. Te enviaremos la confirmación " +
                            "al correo asociado a tu cuenta.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(text = "Aceptar") } }
    )
}
