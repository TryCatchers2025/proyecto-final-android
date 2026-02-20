package com.trycatchers.hotel.data.api

import com.trycatchers.hotel.data.dtos.UserDto
import retrofit2.http.*

interface UserService {

    @GET("users")
    suspend fun getAllUsers(): List<UserDto>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): UserDto

    @POST("users")
    suspend fun createUser(@Body user: UserDto): UserDto

    @PATCH("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: UserDto): UserDto

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String)
}
