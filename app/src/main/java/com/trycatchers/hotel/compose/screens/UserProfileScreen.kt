package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
            onLogout = { viewModel.logout() }
        )
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando datos del usuario...")
        }
    }



}
@Composable
fun UserProfileView(
    user: UserDto,
    photoUrl: String?,
    onNameChange: (String) -> Unit = {},
    onLastNameChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onBirthDateChange: (String) -> Unit = {},
    onSaveChanges: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
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
            ModernTextField(
                label = "Nombre",
                value = user.firstName,
                onValueChange = onNameChange,
            )
        }

        item {
            ModernTextField(
                label = "Apellidos",
                value = user.lastName,
                onValueChange = onLastNameChange,
            )
        }

        item {
            ModernTextField(
                label = "Contraseña",
                value = user.password ?: "",
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = onPasswordChange
            )
        }

        item {
            ModernTextField(
                label = "Fecha de Nacimiento",
                value = user.birthDate,
                onValueChange = onBirthDateChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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

@Composable
fun ModernTextField(
    label: String,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = AppTypography.labelLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            textStyle = MaterialTheme.typography.headlineMedium.copy(color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.inversePrimary,
                disabledTextColor = Color.Black,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}