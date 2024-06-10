package com.cupofcoffee.data.repository

import com.cupofcoffee.data.remote.UserDTO
import com.cupofcoffee.data.remote.UserDataSource

class UserRepositoryImpl(private val userDataSource: UserDataSource) {

    suspend fun insert(id: String, userDTO: UserDTO) = userDataSource.insert(id, userDTO)


    fun getUserByIdInFlow(id: String) = userDataSource.getUserByIdInFlow(id)

    suspend fun getUserById(id: String) = userDataSource.getUserById(id)

    suspend fun update(id: String, userDTO: UserDTO) =
        userDataSource.update(id, userDTO)

    suspend fun delete(id: String) = userDataSource.delete(id)
}