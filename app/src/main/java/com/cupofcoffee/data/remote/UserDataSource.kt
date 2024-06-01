package com.cupofcoffee.data.remote

class UserDataSource(private val userService: UserService) {

    suspend fun insert(id: String, userDTO: UserDTO) = userService.insert(id, userDTO)

    suspend fun getUserById(id: String) = userService.getUserById(id)
}