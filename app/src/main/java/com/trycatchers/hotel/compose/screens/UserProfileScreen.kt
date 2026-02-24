package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.trycatchers.hotel.data.dtos.UserDto
import com.trycatchers.hotel.ui.theme.AppTypography
import com.trycatchers.hotel.utils.ApiConfig
import com.trycatchers.hotel.viewmodels.UserProfileViewModel

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}

) {
    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect { onLogout() }
    }

    val editableUser by viewModel.editableUser.collectAsState()


    editableUser?.let { user ->
        val fullPhotoUrl = user.photo?.let { photo ->
            val base = ApiConfig.BASE_DOMAIN.removeSuffix("/")
            val path = photo.removePrefix("/")
            "$base/$path"
        }

        UserProfileView(
            user = user,
            photoUrl = fullPhotoUrl,
            onNameChange = { viewModel.updateFirstName(it) },
            onLastNameChange = { viewModel.updateLastName(it) },
            onPasswordChange = { viewModel.updatePassword(it) },
            onBirthDateChange = { viewModel.updateBirthDate(it) },
            onSaveChanges = { viewModel.saveChanges() },
            onLogout = { viewModel.logout() },
        )
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando datos del usuario...")
        }
    }



}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileView(
    user: UserDto,
    photoUrl: String?,
    onNameChange: (String) -> Unit = {},
    onLastNameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onBirthDateChange: (String) -> Unit = {},
    onSaveChanges: () -> Unit = {},
    onLogout: () -> Unit = {},

) {
    var passwordVisible by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Bloquea fechas futuras
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

            LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                if (!user.photo.isNullOrEmpty() && !photoUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(photoUrl),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                }

                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }

        item {
            Text("Datos Personales", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }

        item {
            OutlinedTextField(
                value = user.firstName,
                onValueChange = onNameChange,
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }



        item {
            OutlinedTextField(
                value = user.lastName,
                onValueChange = onLastNameChange,
                label = { Text("Apellidos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = user.password,
                onValueChange = onPasswordChange,
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
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de Nacimiento") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Seleccionar fecha"
                                )
                            }
                        }
                    )
                }


        item {
            Text("Información de Contacto", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }

        item {
            InfoCard(label = "Email", value = user.email)
            InfoCard(label = "DNI", value = user.dni)
            InfoCard(label = "Ciudad", value = user.city ?: "No disponible")
            InfoCard(label = "Género", value = user.gender.ifEmpty { "No especificado" })

        }

        item {
            Text("Opciones de Cuenta", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }

        item {
            InfoCard(label = "VIP", value = if (user.vip == true) "Sí" else "No")
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }

        item {
            Button(
                onClick = {
                    onSaveChanges()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar cambios")
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar sesión")
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = java.text.SimpleDateFormat(
                                "dd/MM/yyyy",
                                java.util.Locale.getDefault()
                            )
                            formatter.isLenient = false
                            val formattedDate = formatter.format(java.util.Date(millis))

                            onBirthDateChange(formattedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}



@Composable
fun InfoCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = AppTypography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
