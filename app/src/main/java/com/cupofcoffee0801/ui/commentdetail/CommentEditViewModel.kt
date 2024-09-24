package com.cupofcoffee0801.ui.commentdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.util.NetworkUtil
import com.example.data.model.CommentData
import com.example.data.model.asCommentDTO
import com.example.data.repository.CommentRepository
import com.example.data.repository.MeetingRepository
import com.example.data.repository.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CommentEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val args = CommentEditFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _uiState: MutableLiveData<CommentUiState> =
        MutableLiveData(CommentUiState(isLoading = true))
    val uiState: LiveData<CommentUiState> get() = _uiState

    fun isNetworkConnected() = networkUtil.isConnected()

    init {
        initUiState()
    }

    private fun initUiState() {
        viewModelScope.launch {
            try {
                val uid = Firebase.auth.uid!!
                val user = userRepository.getLocalUserById(uid)!!
                val commentEditUser = CommentEditUser(user.id, user.profileImageWebUrl)
                val commentId = args.commentId
                if (commentId != null) {
                    val comment = commentRepository.getComment(commentId)
                    _uiState.postValue(CommentUiState(commentEditUser, comment))
                } else
                    _uiState.postValue(CommentUiState(commentEditUser, null))
            } catch (e: Exception) {
                _uiState.postValue(CommentUiState(isError = true))
            }
        }
    }

    fun editComment(content: String) {
        viewModelScope.launch {
            val uid = Firebase.auth.uid!!
            val user = userRepository.getLocalUserById(uid)!!
            val comment = CommentData(
                userId = user.id,
                meetingId = args.meetingId,
                nickname = user.nickname,
                profileImageWebUrl = user.profileImageWebUrl,
                content = content,
                createdDate = Date().time
            )
            if (args.commentId == null) insertComment(comment)
            else updateComment(comment)
            completeEdit()
        }
    }

    private suspend fun insertComment(commentData: CommentData) {
        val commentId = commentRepository.insert(commentData.asCommentDTO())
        val meeting = meetingRepository.getMeeting(args.meetingId)
        meeting.commentIds[commentId!!] = true
        meetingRepository.update(meeting)
    }

    private suspend fun updateComment(commentData: CommentData) =
        commentRepository.update(args.commentId!!, commentData.asCommentDTO())

    private fun completeEdit() {
        _uiState.value = uiState.value!!.copy(isCompleted = true)
    }
}