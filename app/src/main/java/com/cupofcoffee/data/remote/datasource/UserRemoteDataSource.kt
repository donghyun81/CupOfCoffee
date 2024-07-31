package com.cupofcoffee.data.remote.datasource

import com.cupofcoffee.data.module.AuthTokenManager.getAuthToken
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
        userService.insert(
            authToken = getAuthToken()!!,
            id = id,
            userDTO = userDTO
        )
    }

    suspend fun getUserById(id: String) = withContext(ioDispatcher) {
        userService.getUserById(
            authToken = getAuthToken() ?: return@withContext null,
            id = id
        )
    }

    suspend fun getUsersByIds(ids: List<String>) = withContext(ioDispatcher) {
        ids.mapNotNull { id ->
            try {
                id to userService.getUserById(
                    authToken = getAuthToken()!!,
                    id = id
                )!!
            } catch (e: Exception) {
                null
            }
        }.toMap()
    }

    suspend fun update(id: String, userDTO: UserDTO) = withContext(ioDispatcher) {
        if (userDTO.madeMeetingIds.isEmpty()) userService.insert(
            id = id,
            authToken = getAuthToken()!!,
            userDTO = userDTO
        )
        else userService.update(
            id = id,
            authToken = getAuthToken()!!,
            userDTO = userDTO
        )
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        userService.delete(
            id = id,
            authToken = getAuthToken()!!,
        )
    }
}