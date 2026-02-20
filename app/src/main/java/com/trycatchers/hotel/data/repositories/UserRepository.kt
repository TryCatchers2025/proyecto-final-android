package com.trycatchers.hotel.data.repositories

import com.trycatchers.hotel.data.api.UserService
import com.trycatchers.hotel.data.dtos.UserDto
import com.trycatchers.hotel.data.dtos.toDomain
import com.trycatchers.hotel.data.models.User
import javax.inject.Inject

class UserRepository @Inject constructor(private val userService: UserService) {

    suspend fun getAll(): List<User> {
        return userService.getAllUsers().toDomain()
    }

    suspend fun getById(id: String): User {
        return userService.getUserById(id).toDomain()
    }

    suspend fun create(user: User): User {
        val dto = user.toDto()
        return userService.createUser(dto).toDomain()
    }

    suspend fun update(id: String, user: User): User {
        val dto = user.toDto()
        return userService.updateUser(id, dto).toDomain()
    }

    suspend fun delete(id: String) {
        userService.deleteUser(id)
    }
}

fun User.toDto() = UserDto(userId = userId, email = email, password = password, name = name)
