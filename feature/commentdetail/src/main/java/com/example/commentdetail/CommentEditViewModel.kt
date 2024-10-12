package com.example.commentdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.util.NetworkUtil
import com.example.data.model.CommentData
import com.example.data.model.asCommentDTO
import com.example.data.repository.CommentRepository
import com.example.data.repository.MeetingRepository
import com.example.data.repository.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

const val EDIT_COMMENT_NETWORK_MESSAGE = "댓글을 추가하거나 수정하기 위해서 네트워크 연결이 필요합니다!"

@HiltViewModel
class CommentEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val args = CommentEditFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _uiState = MutableStateFlow(CommentUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = Channel<CommentEditSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()
    fun handleIntent(intent: CommentEditIntent) {
        when (intent) {
            is CommentEditIntent.EditComment -> {
                viewModelScope.launch {
                    editComment(uiState.value.content)
                    _sideEffect.send(CommentEditSideEffect.NavigateUp)
                }
            }

            CommentEditIntent.InitData -> {
                initUiState()
            }

            is CommentEditIntent.EnterContent -> {
                _uiState.value = uiState.value.copy(
                    content = intent.content
                )
            }
        }
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
                    _uiState.value = CommentUiState(commentEditUser, comment)
                } else
                    _uiState.value = CommentUiState(commentEditUser, null)
            } catch (e: Exception) {
                _uiState.value = CommentUiState(isError = true)
            }
        }
    }

    private fun editComment(content: String) {
        viewModelScope.launch {
            if (networkUtil.isConnected()) {
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
            } else {
                _sideEffect.send(CommentEditSideEffect.ShowSnackBar(EDIT_COMMENT_NETWORK_MESSAGE))
            }
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
}