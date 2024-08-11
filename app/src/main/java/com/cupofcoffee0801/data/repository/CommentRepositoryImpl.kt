package com.cupofcoffee0801.data.repository

import com.cupofcoffee0801.data.remote.datasource.CommentRemoteDataSource
import com.cupofcoffee0801.data.remote.model.CommentDTO
import com.cupofcoffee0801.data.remote.model.asCommentEntry
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentRemoteDataSource: CommentRemoteDataSource
) : CommentRepository {
    override suspend fun insert(commentDTO: CommentDTO): String =
        commentRemoteDataSource.insert(commentDTO)

    override suspend fun getComment(id: String) =
        commentRemoteDataSource.getComment(id).asCommentEntry(id)

    override suspend fun getCommentsByUserId(userId: String) =
        commentRemoteDataSource.getComments().filterValues { it.userId == userId }

    override suspend fun getCommentsByIdsInFlow(ids: List<String>) =
        commentRemoteDataSource.getCommentsByIdsInFlow(ids).map { commentsMap ->
            commentsMap.map { comment ->
                val (id, commentDTO) = comment
                commentDTO.asCommentEntry(id)
            }
        }

    override suspend fun update(id: String, commentDTO: CommentDTO) =
        commentRemoteDataSource.update(id, commentDTO)

    override suspend fun delete(id: String) = commentRemoteDataSource.delete(id)
}