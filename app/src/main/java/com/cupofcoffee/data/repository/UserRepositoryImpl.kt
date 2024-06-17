package com.cupofcoffee.data.repository

import com.cupofcoffee.data.local.UserDao
import com.cupofcoffee.data.remote.UserDTO
import com.cupofcoffee.data.remote.UserDataSource

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val userDataSource: UserDataSource
) {

    suspend fun insertLocal(id: String, userDTO: UserDTO) = userDataSource.insert(id, userDTO)

    suspend fun insertRemote(id: String, userDTO: UserDTO) = userDataSource.insert(id, userDTO)

    fun getLocalUserByIdInFlow(id: String) = userDataSource.getUserByIdInFlow(id)

    fun getRemoteUserByIdInFlow(id: String) = userDataSource.getUserByIdInFlow(id)

    suspend fun getRemoteUserById(id: String) = userDataSource.getUserById(id)

    suspend fun updateLocal(id: String, userDTO: UserDTO) =
        userDataSource.update(id, userDTO)

    suspend fun updateRemote(id: String, userDTO: UserDTO) =
        userDataSource.update(id, userDTO)

    suspend fun deleteLocal(id: String) = userDataSource.delete(id)

    suspend fun deleteRemote(id: String) = userDataSource.delete(id)
}