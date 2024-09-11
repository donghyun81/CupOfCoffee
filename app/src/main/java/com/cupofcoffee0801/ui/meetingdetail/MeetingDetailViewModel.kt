package com.cupofcoffee0801.ui.meetingdetail

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.cupofcoffee0801.data.repository.CommentRepository
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.data.worker.DeleteMeetingWorker
import com.cupofcoffee0801.ui.model.Comment
import com.cupofcoffee0801.ui.model.Meeting
import com.cupofcoffee0801.ui.model.User
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meetingRepository: MeetingRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    val meetingId =
        MeetingDetailFragmentArgs.fromSavedStateHandle(savedStateHandle).meetingId

    private val _meetingDetailUiState: MutableLiveData<MeetingDetailUiState> =
        MutableLiveData(MeetingDetailUiState(isLoading = true))

    val meetingDetailUiState: LiveData<MeetingDetailUiState> get() = _meetingDetailUiState


    var currentJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            currentJob?.cancel()
            currentJob = initUiState()
        }

        override fun onLost(network: Network) {
            currentJob?.cancel()
            currentJob = initUiState()
        }
    }

    init {
        if (isNetworkConnected().not()) {
            currentJob?.cancel()
            currentJob = initUiState()
        }
        networkUtil.registerNetworkCallback(networkCallback)
    }

    fun isNetworkConnected() = networkUtil.isConnected()


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initUiState() = viewModelScope.launch {
        try {
            val meetingInFlow = meetingRepository.getMeetingInFlow(meetingId, isNetworkConnected())
            val user = userRepository.getLocalUserById(Firebase.auth.uid!!)!!
            meetingInFlow
                .flatMapLatest { meeting ->
                    getMeetingDetailUiStateInFlow(meeting!!, user)
                }
                .collect { meetingDetailUiState ->
                    _meetingDetailUiState.postValue(meetingDetailUiState)
                }
        } catch (e: Exception) {
            _meetingDetailUiState.postValue(
                MeetingDetailUiState(isError = true)
            )
        }
    }

    private suspend fun getMeetingDetailUiStateInFlow(meeting: Meeting, user: User) =
        if (isNetworkConnected())
            getCommentsInFlow(meeting.commentIds.keys.toList()).map { comments ->
                MeetingDetailUiState(
                    userUiModel = user.asUserUiModel(),
                    meetingUiModel = meeting.asMeetingUiModel(),
                    comments = comments.map { it.asCommentUiModel() },
                    meeting.managerId == user.id,
                    isLoading = false
                )
            }
        else flow {
            emit(
                MeetingDetailUiState(
                    userUiModel = user.asUserUiModel(),
                    meetingUiModel = meeting.asMeetingUiModel(),
                    comments = emptyList(),
                    meeting.managerId == user.id,
                    isLoading = false
                )
            )
        }


    private suspend fun getCommentsInFlow(ids: List<String>) =
        commentRepository.getCommentsByIdsInFlow(ids)

    fun getDeleteMeetingWorker(meetingId: String): OneTimeWorkRequest {
        val inputData = Data.Builder()
            .putString("meetingId", meetingId)
            .build()

        return OneTimeWorkRequest.Builder(DeleteMeetingWorker::class.java)
            .setInputData(inputData)
            .build()
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            val meeting =
                meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
            meeting.commentIds.remove(commentId)
            meetingRepository.update(meeting)
            commentRepository.delete(commentId)
        }
    }

    private fun User.asUserUiModel() = MeetingDetailUserUiModel(
        id, profileImageWebUrl
    )

    private fun Meeting.asMeetingUiModel() = MeetingDetailMeetingUiModel(
        id,
        content,
        caption,
        date,
        time
    )

    private fun Comment.asCommentUiModel() = MeetingDetailCommentUiModel(
        id,
        nickname,
        profileImageWebUrl,
        content,
        createdDate
    )

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}