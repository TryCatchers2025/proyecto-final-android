package com.trycatchers.hotel.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.api.AuthService
import com.trycatchers.hotel.data.models.LoginRequest
import com.trycatchers.hotel.data.repositories.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("");


    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = authService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        val token = loginResponse.token
                        val payload = token.split(".")[1]
                        val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
                        val role = JSONObject(decoded).optString("role", "")

                        if (role != "customer") {
                            _errorMessage.value = "Acceso no permitido"
                            return@let
                        }

                        sessionRepository.saveSession(token)
                        sessionRepository.getCurrentUser()
                        onSuccess()
                    }
                } else {
                    _errorMessage.value = try {
                        JSONObject(response.errorBody()?.string() ?: "").getString("message")
                    } catch (e: Exception) {
                        "Error en el servidor"
                    }
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Error en la petición: ${e.message}")
            }
        }
    }
}