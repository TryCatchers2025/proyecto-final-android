package com.trycatchers.hotel.data.repositories

import com.trycatchers.hotel.data.api.RoomService
import com.trycatchers.hotel.data.dtos.RoomDto
import com.trycatchers.hotel.data.dtos.toDomain
import com.trycatchers.hotel.data.models.Room
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val roomService: RoomService
) {

    suspend fun getAll(): List<Room> {
        return roomService.getAllRooms().toDomain()
    }

    suspend fun getById(id: String): Room {
        return roomService.getRoomById(id).toDomain()
    }

    suspend fun create(room: Room): Room {
        val dto = room.toDto()
        return roomService.createRoom(dto).toDomain()
    }

    suspend fun update(id: String, room: Room): Room {
        val dto = room.toDto()
        return roomService.updateRoom(id, dto).toDomain()
    }

    suspend fun delete(id: String) {
        roomService.deleteRoom(id)
    }
}

fun Room.toDto() = RoomDto(
    roomId = roomId,
    number = number,
    price = price,
    type = type,
    name = name,
    occupied = occupied,
    limit = limit,
    offer = offer,
    description = description
)