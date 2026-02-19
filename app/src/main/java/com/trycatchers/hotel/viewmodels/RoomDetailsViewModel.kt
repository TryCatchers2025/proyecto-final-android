package com.trycatchers.hotel.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.models.Review
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.ReviewRepository
import com.trycatchers.hotel.data.repositories.RoomRepository
import com.trycatchers.hotel.utils.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomDetailsUiState(
    val isLoading: Boolean = true,
    val room: Room? = null,
    val reviews: List<Review> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class RoomDetailsViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val reviewRepository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val roomId: String = savedStateHandle["roomId"] ?: ""

    private val _uiState = MutableStateFlow(RoomDetailsUiState())
    val uiState: StateFlow<RoomDetailsUiState> = _uiState.asStateFlow()

    init {
        loadRoomDetails()
    }

    fun loadRoomDetails() {
        if (roomId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "ID de habitación no válido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val room = roomRepository.getById(roomId)
                // Also fetch reviews for this room
                val reviews = runCatching { reviewRepository.getByRoom(roomId) }.getOrDefault(emptyList())

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        room = room,
                        reviews = reviews
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.toUserMessage("No se pudo cargar la información de la habitación")
                    )
                }
            }
        }
    }
}
