package com.trycatchers.hotel.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.api.AuthService
import com.trycatchers.hotel.data.dtos.UserDto
import com.trycatchers.hotel.data.repositories.SessionRepository
import com.trycatchers.hotel.utils.uriToFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authService: AuthService,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    init {
        sessionRepository.clearSession()
    }

    private val _editableUser = MutableStateFlow(
        UserDto(
            email = "",
            firstName = "",
            lastName = "",
            password = "",
            birthDate = "",
            gender = "",
            dni = ""
        )
    )
    val editableUser: StateFlow<UserDto> = _editableUser

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    fun updateEmail(value: String) { _editableUser.update { it.copy(email = value) } }
    fun updateFirstName(value: String) { _editableUser.update { it.copy(firstName = value) } }
    fun updateLastName(value: String) { _editableUser.update { it.copy(lastName = value) } }
    fun updatePassword(value: String) { _editableUser.update { it.copy(password = value) } }
    fun updateBirthDate(value: String) { _editableUser.update { it.copy(birthDate = value) } }
    fun updateGender(value: String) { _editableUser.update { it.copy(gender = value) } }
    fun updateDNI(value: String) { _editableUser.update { it.copy(dni = value) } }
    fun updateCity(value: String) { _editableUser.update { it.copy(city = value) } }

    fun register(context: Context, imageUri: Uri?, onSuccess: () -> Unit) {
        _errorMessage.value = ""
        val user = _editableUser.value
        viewModelScope.launch {
            try {
                fun String.toPart() = this.toRequestBody("text/plain".toMediaTypeOrNull())

                val photoPart = imageUri?.let { uri ->
                    val file = uriToFile(context, uri)
                    val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photo", file.name, reqBody)
                }

                val response = authService.register(
                    email = user.email.toPart(),
                    firstName = user.firstName.toPart(),
                    lastName = user.lastName.toPart(),
                    password = user.password.toPart(),
                    birthDate = user.birthDate.toPart(),
                    gender = user.gender.toPart(),
                    dni = user.dni.toPart(),
                    city = user.city?.toPart(),
                    photo = photoPart
                )

                if (response.isSuccessful) {
                    response.body()?.let {
                        sessionRepository.saveSession(it.token)

                        val user = sessionRepository.getCurrentUser()
                        Log.d("REGISTER_DEBUG", "Usuario cargado: $user")


                        onSuccess()
                    } ?: run { _errorMessage.value = "No se recibió respuesta del servidor" }
                } else {
                    val errorRawJson = response.errorBody()?.string()
                    Log.d("REGISTER_DEBUG", "Error raw: $errorRawJson")

                    _errorMessage.value = errorRawJson?.let {
                        try {
                            val json = JSONObject(it)
                            val errorsObj = json.optJSONObject("errors")
                            if (errorsObj != null) {
                                val messages = mutableListOf<String>()
                                errorsObj.keys().forEach { key ->
                                    val arr = errorsObj.optJSONArray(key)
                                    if (arr != null) {
                                        for (i in 0 until arr.length()) {
                                            messages.add(arr.getString(i))
                                        }
                                    }
                                }
                                messages.joinToString("\n")
                            } else {
                                json.optString("message", "Error en el servidor")
                            }
                        } catch (e: Exception) {
                            Log.e("REGISTER_DEBUG", "JSON parse error", e)
                            "Error en el servidor"
                        }
                    } ?: "Error en el servidor"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error en registro"
            }
        }
    }
}