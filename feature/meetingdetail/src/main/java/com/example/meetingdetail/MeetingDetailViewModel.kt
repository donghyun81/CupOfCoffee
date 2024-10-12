package com.example.meetingdetail

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.common.util.NetworkUtil
import com.example.data.model.Comment
import com.example.data.model.Meeting
import com.example.data.model.User
import com.example.data.repository.CommentRepository
import com.example.data.repository.MeetingRepository
import com.example.data.repository.UserRepository
import com.example.work.DeleteMeetingWorker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DELETE_MEETING_NETWORK_MESSAGE = "모임을 삭제하기 위해서 네트워크 연결이 필요합니다!"
private const val DELETE_COMMENT_NETWORK_MESSAGE = "댓글을 삭제하기 위해서 네트워크 연결이 필요합니다!"

@HiltViewModel
class MeetingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meetingRepository: MeetingRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val networkUtil: NetworkUtil,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val meetingId =
        MeetingDetailFragmentArgs.fromSavedStateHandle(savedStateHandle).meetingId

    private val _uiState: MutableStateFlow<MeetingDetailUiState> =
        MutableStateFlow(MeetingDetailUiState(isLoading = true))

    val uiState: StateFlow<MeetingDetailUiState> get() = _uiState.asStateFlow()

    private val _sideEffect = Channel<MeetingDetailSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

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

    fun handleIntent(intent: MeetingDetailIntent) {
        when (intent) {
            is MeetingDetailIntent.DeleteComment -> {
                viewModelScope.launch {
                    if (networkUtil.isConnected()) deleteComment(intent.commentId)
                    else {
                        _sideEffect.send(
                            MeetingDetailSideEffect.ShowSnackBar(
                                DELETE_COMMENT_NETWORK_MESSAGE
                            )
                        )
                    }
                }

            }

            MeetingDetailIntent.DeleteMeeting -> {

                if (networkUtil.isConnected()) deleteMeeting()
                else {
                    viewModelScope.launch {
                        _sideEffect.send(
                            MeetingDetailSideEffect.ShowSnackBar(
                                DELETE_MEETING_NETWORK_MESSAGE
                            )
                        )
                    }
                }
            }

            MeetingDetailIntent.HandleInitData -> {
                try {
                    if (networkUtil.isConnected()) {
                        networkUtil.registerNetworkCallback(networkCallback)
                    } else {
                        currentJob?.cancel()
                        currentJob = initUiState()
                    }
                } catch (e: Exception) {
                    _uiState.value = uiState.value.copy(isError = true)
                }
            }

            is MeetingDetailIntent.EditComment -> {
                viewModelScope.launch {
                    _sideEffect.send(
                        MeetingDetailSideEffect.NavigateToCommentEdit(
                            intent.commentId, intent.meetingId
                        )
                    )
                }

            }

            is MeetingDetailIntent.EditMeeting -> {
                viewModelScope.launch {
                    _sideEffect.send(
                        MeetingDetailSideEffect.NavigateToMakeMeeting(
                            intent.meetingId
                        )
                    )
                }

            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initUiState() = viewModelScope.launch {
        val meetingInFlow =
            meetingRepository.getMeetingInFlow(meetingId, networkUtil.isConnected())
        val user = userRepository.getLocalUserById(Firebase.auth.uid!!)!!
        meetingInFlow
            .flatMapLatest { meeting ->
                getMeetingDetailUiStateInFlow(meeting!!, user)
            }
            .collect { meetingDetailUiState ->
                _uiState.value = meetingDetailUiState
            }
    }

    private suspend fun getMeetingDetailUiStateInFlow(meeting: Meeting, user: User) =
        if (networkUtil.isConnected())
            getCommentsInFlow(meeting.commentIds.keys.toList()).map { comments ->
                MeetingDetailUiState(
                    userUiModel = user.asUserUiModel(),
                    meetingUiModel = meeting.asMeetingUiModel(),
                    comments = comments.map { it.asCommentUiModel() },
                    isMyMeeting = meeting.managerId == user.id,
                    isLoading = false
                )
            }
        else flow {
            emit(
                MeetingDetailUiState(
                    userUiModel = user.asUserUiModel(),
                    meetingUiModel = meeting.asMeetingUiModel(),
                    comments = emptyList(),
                    isMyMeeting = meeting.managerId == user.id,
                    isLoading = false
                )
            )
        }


    private suspend fun getCommentsInFlow(ids: List<String>) =
        commentRepository.getCommentsByIdsInFlow(ids)

    private fun deleteMeeting() {
        val deleteMeetingWorker = getDeleteMeetingWorker(meetingId)
        WorkManager.getInstance(context).enqueue(deleteMeetingWorker)
    }

    private fun getDeleteMeetingWorker(meetingId: String): OneTimeWorkRequest {
        val inputData = Data.Builder()
            .putString("meetingId", meetingId)
            .build()

        return OneTimeWorkRequest.Builder(DeleteMeetingWorker::class.java)
            .setInputData(inputData)
            .build()
    }

    private suspend fun deleteComment(commentId: String) {
        val meeting =
            meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
        meeting.commentIds.remove(commentId)
        meetingRepository.update(meeting)
        commentRepository.delete(commentId)
    }

    private fun User.asUserUiModel() = MeetingDetailUserUiModel(
        id, profileImageWebUrl
    )

    private fun Meeting.asMeetingUiModel() = MeetingDetailMeetingUiModel(
        id,
        content,
        placeName,
        date,
        time
    )

    private fun Comment.asCommentUiModel() = MeetingDetailCommentUiModel(
        id,
        userId == Firebase.auth.uid,
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