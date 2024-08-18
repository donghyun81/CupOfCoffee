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
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.error
import com.cupofcoffee0801.data.DataResult.Companion.loading
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.CommentRepository
import com.cupofcoffee0801.data.repository.MeetingRepository
import com.cupofcoffee0801.data.repository.UserRepository
import com.cupofcoffee0801.data.worker.DeleteMeetingWorker
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    private val _meetingDetailDataResult: MutableLiveData<DataResult<MeetingDetailUiState>> =
        MutableLiveData(
            loading()
        )

    val meetingDetailDataResult: LiveData<DataResult<MeetingDetailUiState>> get() = _meetingDetailDataResult

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


    private fun initUiState() = viewModelScope.launch {
        try {
            val meetingEntryInFlow =
                meetingRepository.getMeetingInFlow(meetingId, isNetworkConnected())
            val userEntry = userRepository.getLocalUserById(Firebase.auth.uid!!)!!
            meetingEntryInFlow
                .flatMapLatest { meetingEntry ->
                    meetingEntry!!
                    if (isNetworkConnected()) {
                        getCommentsInFlow(meetingEntry.meetingModel.commentIds.keys.toList()).map { commentEntries ->
                            MeetingDetailUiState(
                                userEntry,
                                meetingEntry,
                                commentEntries,
                                meetingEntry.meetingModel.managerId == userEntry.id
                            )
                        }
                    } else flow {
                        emit(
                            MeetingDetailUiState(
                                userEntry,
                                meetingEntry,
                                emptyList(),
                                meetingEntry.meetingModel.managerId == userEntry.id
                            )
                        )
                    }
                }
                .collect { meetingDetailUiState ->
                    _meetingDetailDataResult.postValue(success(meetingDetailUiState))
                }
        } catch (e: Exception) {
            _meetingDetailDataResult.postValue(error(e))
        }
    }

    private suspend fun getCommentsInFlow(ids: List<String>) =
        commentRepository.getCommentsByIdsInFlow(ids)

    fun getDeleteMeetingWorker(meetingEntry: MeetingEntry): OneTimeWorkRequest {
        val jsonMeetingEntry = Json.encodeToString(meetingEntry)
        val inputData = Data.Builder()
            .putString("meetingEntry", jsonMeetingEntry)
            .build()

        return OneTimeWorkRequest.Builder(DeleteMeetingWorker::class.java)
            .setInputData(inputData)
            .build()
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            val meetingEntry =
                meetingRepository.getMeeting(meetingId, networkUtil.isConnected())
            meetingEntry.meetingModel.commentIds.remove(commentId)
            meetingRepository.update(meetingEntry)
            commentRepository.delete(commentId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}