package com.cupofcoffee0801.ui.commentdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.CommentRepository
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.ui.model.CommentModel
import com.cupofcoffee0801.ui.model.asCommentDTO
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    val args = CommentEditFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _commentEditDataResult: MutableLiveData<DataResult<CommentEditUiState>> =
        MutableLiveData(loading())
    val commentEditDataResult: LiveData<DataResult<CommentEditUiState>> get() = _commentEditDataResult

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked

    fun isNetworkConnected() = networkUtil.isConnected()

    init {
        initUiState()
    }

    fun onButtonClicked() {
        _isButtonClicked.value = true
    }

    private fun initUiState() {
        viewModelScope.launch {
            try {
                val uid = Firebase.auth.uid!!
                val user = userRepository.getLocalUserById(uid)!!
                val commentId = args.commentId
                if (commentId != null) {
                    val comment = commentRepository.getComment(commentId).commentModel
                    _commentEditDataResult.postValue(success(CommentEditUiState(user, comment)))
                } else
                    _commentEditDataResult.postValue(success(CommentEditUiState(user, null)))
            } catch (e: Exception) {
                _commentEditDataResult.postValue(error(e))
            }
        }
    }

    suspend fun insertComment(commentModel: CommentModel) {
        val commentId = commentRepository.insert(commentModel.asCommentDTO())
        val meeting = meetingRepository.getMeeting(args.meetingId)
        meeting.meetingModel.commentIds[commentId!!] = true
        meetingRepository.update(meeting)
    }

    suspend fun updateComment(commentModel: CommentModel) {
        commentRepository.update(args.commentId!!, commentModel.asCommentDTO())
    }
}