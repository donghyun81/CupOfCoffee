package com.cupofcoffee0801.data.repository

import com.cupofcoffee0801.data.remote.RemoteIdWrapper
import com.cupofcoffee0801.data.remote.datasource.CommentRemoteDataSource
import com.cupofcoffee0801.data.remote.model.CommentDTO
import com.cupofcoffee0801.data.remote.model.asCommentEntry
import kotlinx.coroutines.flow.map

class CommentRepositoryImpl(
    private val commentRemoteDataSource: CommentRemoteDataSource
) {
    suspend fun insert(commentDTO: CommentDTO): RemoteIdWrapper =
        commentRemoteDataSource.insert(commentDTO)

    suspend fun getComment(id: String) = commentRemoteDataSource.getComment(id).asCommentEntry(id)

    suspend fun getCommentsByUserId() =
        commentRemoteDataSource.getComments()

    suspend fun getCommentsByIdsInFlow(ids: List<String>) =
        commentRemoteDataSource.getCommentsByIdsInFlow(ids).map { commentsMap ->
            commentsMap.map { comment ->
                val (id, commentDTO) = comment
                commentDTO.asCommentEntry(id)
            }
        }

    suspend fun update(id: String, commentDTO: CommentDTO) =
        commentRemoteDataSource.update(id, commentDTO)

    suspend fun delete(id: String) = commentRemoteDataSource.delete(id)
}