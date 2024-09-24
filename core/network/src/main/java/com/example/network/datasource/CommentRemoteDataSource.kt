package com.example.network.datasource

import com.example.common.di.AuthTokenManager.getAuthToken
import com.example.common.di.IoDispatcher
import com.example.common.di.RefreshInterval
import com.example.network.model.CommentDTO
import com.example.network.service.CommentService
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