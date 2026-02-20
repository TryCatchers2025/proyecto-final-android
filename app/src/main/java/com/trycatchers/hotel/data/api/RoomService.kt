package com.trycatchers.hotel.data.api

import com.trycatchers.hotel.data.dtos.RoomDto
import retrofit2.http.*

interface RoomService {

    @GET("rooms")
    suspend fun getAllRooms(): List<RoomDto>

    @GET("rooms")
    suspend fun searchAvailableRooms(@QueryMap filters: Map<String, String>): List<RoomDto>

    @GET("rooms/{id}")
    suspend fun getRoomById(@Path("id") id: String): RoomDto

    @POST("rooms")
    suspend fun createRoom(@Body room: RoomDto): RoomDto

    @PATCH("rooms/{id}")
    suspend fun updateRoom(@Path("id") id: String, @Body room: RoomDto): RoomDto

    @DELETE("rooms/{id}")
    suspend fun deleteRoom(@Path("id") id: String)
}
