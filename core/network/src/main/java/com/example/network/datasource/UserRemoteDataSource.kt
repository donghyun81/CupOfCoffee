package com.example.network.datasource

import com.example.common.di.AuthTokenManager.getAuthToken
import com.example.common.di.IoDispatcher
import com.example.network.model.UserDTO
import com.example.network.service.UserService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRemoteDataSource @Inject constructor(
    private val userService: UserService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun insert(id: String, userDTO: UserDTO) = withContext(ioDispatcher) {
        userService.insert(
            authToken = getAuthToken()!!,
            id = id,
            userDTO = userDTO
        ).id
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