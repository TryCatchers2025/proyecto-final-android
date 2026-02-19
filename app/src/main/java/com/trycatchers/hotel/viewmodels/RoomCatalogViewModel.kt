package com.trycatchers.hotel.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.RoomRepository
import com.trycatchers.hotel.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomCatalogUiState(
    val isLoading: Boolean = true,
    val rooms: List<Room> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class RoomCatalogViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomCatalogUiState())
    val uiState: StateFlow<RoomCatalogUiState> = _uiState.asStateFlow()

    init {
        loadRooms()
    }

    fun loadRooms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rooms = roomRepository.getAll()
                _uiState.update { it.copy(isLoading = false, rooms = rooms) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.toUserMessage("No se pudo cargar el catálogo de habitaciones")
                    )
                }
            }
        }
    }
}
