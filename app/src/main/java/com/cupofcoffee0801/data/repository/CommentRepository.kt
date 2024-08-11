package com.cupofcoffee0801.data.repository

import com.cupofcoffee0801.data.remote.model.CommentDTO
import com.cupofcoffee0801.ui.model.CommentEntry
import kotlinx.coroutines.flow.Flow

interface CommentRepository {

    suspend fun insert(commentDTO: CommentDTO): String?
    suspend fun getComment(id: String): CommentEntry
    suspend fun getCommentsByUserId(userId: String): Map<String, CommentDTO>
    suspend fun getCommentsByIdsInFlow(ids: List<String>): Flow<List<CommentEntry>>

    suspend fun update(id: String, commentDTO: CommentDTO)

    suspend fun delete(id: String)
}