package com.trycatchers.hotel.compose.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.trycatchers.hotel.utils.formatDate

@Composable
fun DatePickerRange(
    startDate: Long? = null,
    endDate: Long? = null,
    labelTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    modifier: Modifier = Modifier,
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit
) {
    var startDateMillis by remember { mutableStateOf(startDate) }
    var endDateMillis by remember { mutableStateOf(endDate) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var hasInteracted by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        DateRangeFields(
            startDate = formatDate(startDateMillis),
            endDate = formatDate(endDateMillis),
            hasInteracted = hasInteracted,
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            labelTextStyle = labelTextStyle,
            onFieldsClick = {
                hasInteracted = true
                showDateRangePicker = true
            }
        )

        if (showDateRangePicker) {
            DateRangePickerModal(
                initialStartDate = startDateMillis,
                initialEndDate = endDateMillis,
                onDateRangeSelected = { startMillis, endMillis ->
                    startDateMillis = startMillis
                    endDateMillis = endMillis
                    showDateRangePicker = false
                    onDateRangeSelected(Pair(startMillis, endMillis))
                },
                onDismiss = { showDateRangePicker = false }
            )
        }
    }
}

@Composable
private fun DateRangeFields(
    startDate: String,
    endDate: String,
    hasInteracted: Boolean,
    startDateMillis: Long?,
    endDateMillis: Long?,
    labelTextStyle: TextStyle,
    onFieldsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showError = hasInteracted && (startDateMillis == null || endDateMillis == null)
    val borderColor =
        if (showError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline

    val labelColor =
        if (showError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier.fillMaxWidth().clickable(onClick = onFieldsClick)) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.Transparent,
            border = BorderStroke(width = 1.dp, color = borderColor),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "Check in",
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                        )
                        Text(
                            text = startDate.ifEmpty { "DD/MM/YYYY" },
                            style = labelTextStyle,
                            color = labelColor
                        )
                    }
                }

                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().width(1.dp),
                    thickness = 1.dp,
                    color = borderColor.copy(alpha = 0.3f)
                )

                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "Fecha vuelta",
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                        )
                        Text(
                            text = endDate.ifEmpty { "DD/MM/YYYY" },
                            style = labelTextStyle,
                            color = labelColor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerModal(
    initialStartDate: Long? = null,
    initialEndDate: Long? = null,
    onDateRangeSelected: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState =
        rememberDateRangePickerState(
            initialSelectedStartDateMillis = initialStartDate,
            initialSelectedEndDateMillis = initialEndDate
        )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            ConfirmButton(
                onConfirm = {
                    onDateRangeSelected(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                    onDismiss()
                }
            )
        },
        dismissButton = { DismissButton(onDismiss = onDismiss) }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = { Text("Seleccionar fechas") },
            headline = {
                DateRangeHeadline(
                    startDateMillis = dateRangePickerState.selectedStartDateMillis,
                    endDateMillis = dateRangePickerState.selectedEndDateMillis
                )
            },
            showModeToggle = false,
            modifier = Modifier.fillMaxWidth().height(500.dp).padding(16.dp)
        )
    }
}

@Composable
private fun DateRangeHeadline(startDateMillis: Long?, endDateMillis: Long?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = startDateMillis?.let { formatDate(it) } ?: "Check-In",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = endDateMillis?.let { formatDate(it) } ?: "Check-Out",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ConfirmButton(onConfirm: () -> Unit) {
    TextButton(onClick = onConfirm) { Text("OK") }
}

@Composable
private fun DismissButton(onDismiss: () -> Unit) {
    TextButton(onClick = onDismiss) { Text("Cancelar") }
}

@Preview
@Composable
fun DatePickerRangePreview() {
    DatePickerRange(onDateRangeSelected = {})
}
