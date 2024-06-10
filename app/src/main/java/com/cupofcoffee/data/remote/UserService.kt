package com.cupofcoffee.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

private const val USER_PATH = "users"

interface UserService {

    @PUT("$USER_PATH/{id}.json")
    suspend fun insert(@Path("id") id: String, @Body userDTO: UserDTO): RemoteIdWrapper

    @GET("$USER_PATH/{id}.json")
    suspend fun getUserById(@Path("id") id: String): UserDTO

    @PATCH("$USER_PATH/{id}.json")
    suspend fun update(
        @Path("id") id: String,
        @Body userDTO: UserDTO
    )

    @DELETE("$USER_PATH/{id}.json")
    suspend fun delete(
        @Path("id") id: String
    )
}