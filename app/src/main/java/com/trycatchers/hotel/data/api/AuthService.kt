package com.trycatchers.hotel.data.api

import com.trycatchers.hotel.data.dtos.UserDto
import com.trycatchers.hotel.data.models.LoginRequest
import com.trycatchers.hotel.data.models.LoginResponse
import com.trycatchers.hotel.data.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthService {
    @POST("auth/login") suspend fun login(@Body user: LoginRequest): Response<LoginResponse>

    @Multipart
    @POST("/api/auth/register")
    suspend fun register(
        @Part("email") email: RequestBody,
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("password") password: RequestBody,
        @Part("birthDate") birthDate: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("dni") dni: RequestBody,
        @Part("city") city: RequestBody?,
        @Part photo: MultipartBody.Part?
    ): Response<LoginResponse>

}