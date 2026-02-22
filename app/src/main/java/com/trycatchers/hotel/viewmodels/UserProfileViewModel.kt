package com.trycatchers.hotel.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.dtos.UserDto
import com.trycatchers.hotel.data.repositories.SessionRepository
import com.trycatchers.hotel.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserProfileViewModel  @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _editableUser = MutableStateFlow<UserDto?>(null)
    val editableUser: StateFlow<UserDto?> = _editableUser

    val currentUser: StateFlow<UserDto?> = sessionRepository.currentUser


    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionRepository.currentUser.collect { user ->
                _editableUser.value = user
            }
        }
    }

    fun updateFirstName(newName: String) {
        _editableUser.update { it?.copy(firstName = newName) }
    }

    fun updateLastName(newLastName: String) {
        _editableUser.update { it?.copy(lastName = newLastName) }
    }

    fun updatePassword(newPassword: String) {
        _editableUser.update { it?.copy(password = newPassword) }
    }

    fun updateBirthDate(newBirthDate: String) {
        _editableUser.update { it?.copy(birthDate = newBirthDate) }
    }

    fun saveChanges(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val userToSave = _editableUser.value ?: return
        val id = userToSave.userId ?: run {
            onError("ID de usuario no disponible")
            return
        }

        viewModelScope.launch {
            try {
                val updatedUserDto = userRepository.update(
                    id = id,
                    user = userToSave.toDomain()
                )
                sessionRepository.updateUser(updatedUserDto)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al guardar cambios")
            }
        }
    }
    fun logout() {
        sessionRepository.clearSession()
        _editableUser.value = null
        viewModelScope.launch {
            _logoutEvent.emit(Unit)
        }
    }
}
