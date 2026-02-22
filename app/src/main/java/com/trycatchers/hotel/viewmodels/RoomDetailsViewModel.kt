package com.trycatchers.hotel.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomDetailsViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    var room by mutableStateOf<Room?>(null)
        private set

    val roomId: String = savedStateHandle["roomId"] ?: ""

    init {
        loadRoom()
    }

    private fun loadRoom() {
        viewModelScope.launch {
            try {
                room = roomRepository.getById(roomId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
