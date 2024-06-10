package com.cupofcoffee.data.remote

import com.cupofcoffee.ui.model.PlaceEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

class UserDataSource(
    private val userService: UserService,
    private val refreshIntervalMs: Long = 3000
) {

    suspend fun insert(id: String, userDTO: UserDTO) = userService.insert(id, userDTO)

    fun getUserByIdInFlow(id: String): Flow<UserDTO?> {
        return flow {
            try {
                while (true) {
                    userService.getUserById(id)
                    emit(userService.getUserById(id))
                    delay(refreshIntervalMs)
                }
            } catch (e: Exception) {
                emit(null)
            }
        }
    }

    suspend fun getUserById(id: String) = userService.getUserById(id)


    suspend fun update(id: String, userDTO: UserDTO) =
        userService.update(id, userDTO)

    suspend fun delete(id: String) = userService.delete(id)

}