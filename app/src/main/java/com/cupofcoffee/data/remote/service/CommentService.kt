package com.cupofcoffee.data.remote.service

import com.cupofcoffee.data.remote.RemoteIdWrapper
import com.cupofcoffee.data.remote.model.CommentDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

private const val COMMENT_PATH = "comments"

interface CommentService {

    @POST("$COMMENT_PATH.json")
    suspend fun insert(@Body commentDTO: CommentDTO): RemoteIdWrapper

    @GET("$COMMENT_PATH/{id}.json")
    suspend fun getComment(@Path("id") id: String): CommentDTO

    @PATCH("$COMMENT_PATH/{id}.json")
    suspend fun update(@Path("id") id: String, @Body commentDTO: CommentDTO)

    @DELETE("$COMMENT_PATH/{id}.json")
    suspend fun delete(@Path("id") id: String)
}