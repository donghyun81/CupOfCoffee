package com.cupofcoffee.ui.meetingdetail

import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.DataResult.Companion.error
import com.cupofcoffee.data.DataResult.Companion.loading
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.repository.CommentRepositoryImpl
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.data.worker.DeleteMeetingWorker
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.asMeetingEntity
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MeetingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val commentRepositoryImpl: CommentRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    val meetingId =
        MeetingDetailFragmentArgs.fromSavedStateHandle(savedStateHandle).meetingId

    private val _meetingDetailUiState: MutableLiveData<DataResult<MeetingDetailUiState>> =
        MutableLiveData(
            loading()
        )

    val meetingDetailUiState: LiveData<DataResult<MeetingDetailUiState>> get() = _meetingDetailUiState

    private var currentJob: Job? = null

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
        networkUtil.registerNetworkCallback(networkCallback)
    }


    private fun initUiState() = viewModelScope.launch {
        try {
            val meetingEntryInFlow =
                meetingRepositoryImpl.getMeetingInFlow(meetingId, networkUtil.isConnected())
            val userEntry = userRepositoryImpl.getLocalUserById(Firebase.auth.uid!!)
            meetingEntryInFlow
                .flatMapLatest { meetingEntry ->
                    meetingEntry!!
                    getCommentsInFlow(meetingEntry.meetingModel.commentIds.keys.toList()).map { commentEntries ->
                        MeetingDetailUiState(
                            userEntry,
                            meetingEntry,
                            commentEntries,
                            meetingEntry.meetingModel.managerId == userEntry.id
                        )
                    }
                }
                .collect { meetingDetailUiState ->
                    _meetingDetailUiState.postValue(success(meetingDetailUiState))
                }
        } catch (e: Exception) {
            _meetingDetailUiState.postValue(error(e))
        }
    }

    private suspend fun getCommentsInFlow(ids: List<String>) =
        commentRepositoryImpl.getCommentsByIdsInFlow(ids)

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
                meetingRepositoryImpl.getMeeting(meetingId, networkUtil.isConnected())
            meetingEntry.meetingModel.commentIds.remove(commentId)
            meetingRepositoryImpl.update(meetingEntry)
            commentRepositoryImpl.delete(commentId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MeetingDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    commentRepositoryImpl = CupOfCoffeeApplication.commentRepository,
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}