package com.cupofcoffee.data.repository

import com.cupofcoffee.data.remote.UserDTO
import com.cupofcoffee.data.remote.UserDataSource

class UserRepositoryImpl(private val userDataSource: UserDataSource) {

    suspend fun insert(id: String, userDTO: UserDTO) = userDataSource.insert(id, userDTO)


    suspend fun getUserById(id: String) = userDataSource.getUserById(id)
}