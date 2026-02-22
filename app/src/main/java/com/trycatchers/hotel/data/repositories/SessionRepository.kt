package com.trycatchers.hotel.data.repositories

import android.content.Context
import android.util.Log
import com.trycatchers.hotel.data.api.UserService
import com.trycatchers.hotel.data.dtos.UserDto
import com.trycatchers.hotel.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.core.content.edit

@Singleton
class SessionRepository @Inject constructor(
    private val userService: Provider<UserService>,
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)


    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    fun saveSession(token: String) {
        Log.d("SESSION_REPO", "Guardando nuevo token: $token")
        prefs.edit(commit = true) {
            remove("auth_token")
            putString("auth_token", token)
        }
    }

    suspend fun getCurrentUser(): UserDto? {
        return try {
            val response = userService.get().getMe()
            if (response.isSuccessful) {
                val user = response.body()
                _currentUser.value = user
                user
            } else {
                Log.e("SessionRepo", "Error API: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("SessionRepo", "Fallo: ${e.message}")
            null
        }
    }

    fun updateUser(user: UserDto?) {
        _currentUser.value = user
    }

    fun clearSession() {
        prefs.edit { remove("auth_token") }
        _currentUser.value = null
    }

    fun getToken(): String? {
        val token = prefs.getString("auth_token", null)
        Log.d("SESSION_REPO", "Token leído de SharedPreferences: $token")
        return token
    }
    fun isLoggedIn(): Boolean {
        return prefs.getString("auth_token", null) != null
    }

    fun getTokenFromPrefs(): String? {
        val token = prefs.getString("auth_token", null)
        Log.d("SESSION_REPO", "Token leído de SharedPreferences: $token")
        return token
    }

}

