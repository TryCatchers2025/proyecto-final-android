package com.trycatchers.hotel.compose.components.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDown(
    item: T?,
    onItemSelected: (T) -> Unit,
    label: String,
    items: List<T>,
    itemToString: (T) -> String = { it.toString() },
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = item?.let { itemToString(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEach { selectionOption ->
                    DropdownMenuItem(text = { Text(itemToString(selectionOption)) }, onClick = {
                        onItemSelected(selectionOption)
                        expanded = false
                    })
                }
            }
        }
    }
}

@Preview
@Composable
fun DropDownPreview() {
    var selected by remember { mutableStateOf("Option 1") }
    DropDown(
        item = selected,
        onItemSelected = { selected = it },
        label = "Selecciona una opción",
        items = listOf("1", "2", "3")
    )
}
