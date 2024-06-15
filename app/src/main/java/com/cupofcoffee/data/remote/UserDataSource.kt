package com.cupofcoffee.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserDataSource(
    private val userService: UserService,
    private val refreshIntervalMs: Long = 3000
) {

    suspend fun insert(id: String, userDTO: UserDTO) = userService.insert(id, userDTO)

    fun getUserByIdInFlow(id: String): Flow<UserDTO> {
        return flow {
            try {
                while (true) {
                    userService.getUserById(id)
                    emit(userService.getUserById(id))
                    delay(refreshIntervalMs)
                }
            } catch (e: Exception) {
                require(false) { e.toString() }
            }
        }
    }

    suspend fun getUserById(id: String) = userService.getUserById(id)


    suspend fun update(id: String, userDTO: UserDTO) =
        userService.update(id, userDTO)

    suspend fun delete(id: String) = userService.delete(id)

}