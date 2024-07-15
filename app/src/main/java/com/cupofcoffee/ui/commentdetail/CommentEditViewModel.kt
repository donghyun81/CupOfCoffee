package com.cupofcoffee.ui.commentdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.DataResult.Companion.error
import com.cupofcoffee.data.DataResult.Companion.loading
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.repository.CommentRepositoryImpl
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.CommentModel
import com.cupofcoffee.ui.model.asCommentDTO
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class CommentEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val commentRepositoryImpl: CommentRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    val args = CommentEditFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _commentEditUiState: MutableLiveData<DataResult<CommentEditUiState>> =
        MutableLiveData(loading())
    val commentEditUiState: LiveData<DataResult<CommentEditUiState>> = _commentEditUiState
    fun isNetworkConnected() = networkUtil.isConnected()

    init {
        initUiState()
    }

    private fun initUiState() {
        viewModelScope.launch {
            try {
                val uid = Firebase.auth.uid!!
                val user = userRepositoryImpl.getLocalUserById(uid)
                val commentId = args.commentId
                if (commentId != null) {
                    val comment = commentRepositoryImpl.getComment(commentId).commentModel
                    _commentEditUiState.postValue(success(CommentEditUiState(user, comment)))
                } else
                    _commentEditUiState.postValue(success(CommentEditUiState(user, null)))
            } catch (e: Exception) {
                _commentEditUiState.postValue(error(e))
            }
        }
    }

    suspend fun insertComment(commentModel: CommentModel) {
        val commentId = commentRepositoryImpl.insert(commentModel.asCommentDTO())
        val meeting = meetingRepositoryImpl.getMeeting(args.meetingId)
        meeting.meetingModel.commentIds[commentId.name] = true
        meetingRepositoryImpl.update(meeting)
    }

    suspend fun updateComment(commentModel: CommentModel) {
        commentRepositoryImpl.update(args.commentId!!, commentModel.asCommentDTO())
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CommentEditViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    commentRepositoryImpl = CupOfCoffeeApplication.commentRepository,
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}