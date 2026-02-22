package com.trycatchers.hotel.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.data.repositories.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RoomCatalogViewModel @Inject constructor(
    private val repository: RoomRepository
) : ViewModel(){

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())

    val rooms: StateFlow<List<Room>> = combine(_searchText, _rooms) { text, rooms ->
        if (text.isBlank()) {
            rooms
        } else {
            rooms.filter { room ->
                room.name.contains(text, ignoreCase = true)
            }
        }
    }

        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = _rooms.value
        )

    init {
        loadRooms()
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    fun loadRooms() {
        viewModelScope.launch {
            try {

                val result = repository.getAll()
                Log.d("ROOMS", "Habitaciones: ${result.size}")
                _rooms.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ROOMS", "Error cargando habitaciones", e)
            }
        }
    }
}
