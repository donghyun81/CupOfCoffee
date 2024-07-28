package com.cupofcoffee.data.remote.datasource

import com.cupofcoffee.data.module.AuthTokenManager.getAuthToken
import com.cupofcoffee.data.remote.RemoteIdWrapper
import com.cupofcoffee.data.remote.model.CommentDTO
import com.cupofcoffee.data.remote.service.CommentService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class CommentRemoteDataSource(
    private val commentService: CommentService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val refreshIntervalMs: Long = 3000L
) {

    suspend fun insert(commentDTO: CommentDTO): RemoteIdWrapper = withContext(ioDispatcher) {
        commentService.insert(getAuthToken()!!, commentDTO)
    }

    suspend fun getComment(id: String): CommentDTO = withContext(ioDispatcher) {
        commentService.getComment(
            id = id,
            authToken = getAuthToken()!!
        )
    }

    suspend fun getComments(): Map<String, CommentDTO> = withContext(ioDispatcher) {
        try {
            commentService.getComments(
                authToken = getAuthToken()!!,
            )
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getCommentsByIdsInFlow(ids: List<String>): Flow<Map<String, CommentDTO>> =
        withContext(ioDispatcher) {
            flow {
                while (true) {
                    emit(tryGetComments(ids = ids))
                    delay(refreshIntervalMs)
                }
            }
        }

    private suspend fun tryGetComments(ids: List<String>): Map<String, CommentDTO> {
        return try {
            ids.associateWith { id ->
                commentService.getComment(
                    id = id,
                    authToken = getAuthToken()!!
                )
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun update(id: String, commentDTO: CommentDTO) = withContext(ioDispatcher) {
        commentService.update(
            id = id,
            authToken = getAuthToken()!!,
            commentDTO = commentDTO
        )
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        commentService.delete(
            id = id,
            authToken = getAuthToken()!!
        )
    }
}