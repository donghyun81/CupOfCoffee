package com.example.data.repository


import com.example.data.model.Comment
import com.example.network.model.CommentDTO
import kotlinx.coroutines.flow.Flow

interface CommentRepository {

    suspend fun insert(commentDTO: CommentDTO): String?
    suspend fun getComment(id: String): Comment
    suspend fun getCommentsByUserId(userId: String): Map<String, CommentDTO>
    suspend fun getCommentsByIdsInFlow(ids: List<String>): Flow<List<Comment>>

    suspend fun update(id: String, commentDTO: CommentDTO)

    suspend fun delete(id: String)
}