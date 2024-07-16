package com.cupofcoffee.data.remote.datasource

import android.util.Log
import com.cupofcoffee.data.remote.RemoteIdWrapper
import com.cupofcoffee.data.remote.model.CommentDTO
import com.cupofcoffee.data.remote.model.PlaceDTO
import com.cupofcoffee.data.remote.service.CommentService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

class CommentRemoteDataSource(
    private val commentService: CommentService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val refreshIntervalMs: Long = 3000L
) {

    suspend fun insert(commentDTO: CommentDTO): RemoteIdWrapper = withContext(ioDispatcher) {
        commentService.insert(commentDTO)
    }

    suspend fun getComment(id: String): CommentDTO = withContext(ioDispatcher) {
        commentService.getComment(id)
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
                commentService.getComment(id)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun update(id: String, commentDTO: CommentDTO) = withContext(ioDispatcher) {
        commentService.update(id, commentDTO)
    }

    suspend fun delete(id: String) = withContext(ioDispatcher) {
        commentService.delete(id)
    }
}