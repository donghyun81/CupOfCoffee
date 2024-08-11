package com.cupofcoffee0801.data.remote.datasource

import com.cupofcoffee0801.data.module.AuthTokenManager.getAuthToken
import com.cupofcoffee0801.data.module.IoDispatcher
import com.cupofcoffee0801.data.module.RefreshInterval
import com.cupofcoffee0801.data.remote.model.CommentDTO
import com.cupofcoffee0801.data.remote.service.CommentService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentRemoteDataSource @Inject constructor(
    private val commentService: CommentService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @RefreshInterval private val refreshIntervalMs: Long
) {

    suspend fun insert(commentDTO: CommentDTO) = withContext(ioDispatcher) {
        commentService.insert(getAuthToken()!!, commentDTO).id
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