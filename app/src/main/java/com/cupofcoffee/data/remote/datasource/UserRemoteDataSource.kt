package com.cupofcoffee.data.remote.datasource

import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.remote.model.UserDTO
import com.cupofcoffee.data.remote.service.UserService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRemoteDataSource(
    private val userService: UserService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun insert(id: String, userDTO: UserDTO) = withContext(ioDispatcher) {
        userService.insert(id, userDTO)
    }

    suspend fun getUserById(id: String) = withContext(ioDispatcher) {
        DataResult.Success(userService.getUserById(id))
    }

    suspend fun getUsersByIds(ids: List<String>) = withContext(ioDispatcher) {
        val users = ids.associateWith { id -> userService.getUserById(id)!! }
        users
    }

    suspend fun update(id: String, userDTO: UserDTO) = withContext(ioDispatcher) {
        userService.update(id, userDTO)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        userService.delete(id)
    }
}