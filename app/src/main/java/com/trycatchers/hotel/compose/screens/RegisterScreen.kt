package com.trycatchers.hotel.compose.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trycatchers.hotel.viewmodels.RegisterViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.VisualTransformation
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit = {},
) {
    val user by viewModel.editableUser.collectAsState()

    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    val genderOptions = listOf("masculino", "femenino", "prefiero no decirlo")
    var genderExpanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Text("Registro de Usuario", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            OutlinedTextField(
                value = user.firstName,
                onValueChange = { viewModel.updateFirstName(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = user.lastName,
                onValueChange = { viewModel.updateLastName(it) },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = user.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }

        item {
            OutlinedTextField(
                value = user.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Contraseña") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
            )
        }

        item {
            OutlinedTextField(
                value = user.birthDate,
                onValueChange = { viewModel.updateBirthDate(it) },
                label = { Text("Fecha de Nacimiento (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = user.gender.ifEmpty { "Selecciona un género" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Género") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.updateGender(option)
                                genderExpanded = false
                            }
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = user.dni,
                onValueChange = { viewModel.updateDNI(it) },
                label = { Text("DNI") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = user.city ?: "",
                onValueChange = { viewModel.updateCity(it) },
                label = { Text("Ciudad (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Seleccionar foto")
            }
        }


        item {
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.register(
                        context = context,
                        imageUri = imageUri, // coincide con el ViewModel
                        onSuccess = { onRegisterSuccess() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Registrarse")
            }
        }

        item {
            imageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Foto seleccionada",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(top = 8.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}