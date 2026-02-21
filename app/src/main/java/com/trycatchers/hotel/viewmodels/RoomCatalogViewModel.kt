package com.trycatchers.hotel.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RoomCatalogViewModel @Inject constructor(
    private val repository: RoomRepository
) : ViewModel(){

    private val _rooms = mutableStateOf<List<Room>>(emptyList())
    val rooms: State<List<Room>> = _rooms

    init {
        loadRooms()
    }

    fun loadRooms() {
        viewModelScope.launch {
            try {
                val result = repository.getAll()
                Log.d("ROOMS", "Habitaciones: ${result.size}")
                _rooms.value = repository.getAll()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ROOMS", "Error cargando habitaciones", e)
            }
        }
    }
}
