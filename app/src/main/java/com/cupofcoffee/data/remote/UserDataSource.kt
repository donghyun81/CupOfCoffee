package com.cupofcoffee.data.remote

import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

class UserDataSource(private val userService: UserService) {

    suspend fun insert(id: String, userDTO: UserDTO) = userService.insert(id, userDTO)

    suspend fun getUserById(id: String) = userService.getUserById(id)

    suspend fun update(id: String, userDTO: UserDTO) =
        userService.update(id, userDTO)
}