package com.trycatchers.hotel.data.api

import com.trycatchers.hotel.data.dtos.RoomDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface RoomService {

    @GET("api/rooms")
    suspend fun getAllRooms(): List<RoomDto>

    @GET("api/rooms/{id}")
    suspend fun getRoomById(@Path("id") id: String): RoomDto

    @POST("api/rooms")
    suspend fun createRoom(@Body room: RoomDto): RoomDto

    @PATCH("api/rooms/{id}")
    suspend fun updateRoom(@Path("id") id: String, @Body room: RoomDto): RoomDto

    @DELETE("api/rooms/{id}")
    suspend fun deleteRoom(@Path("id") id: String)
}
