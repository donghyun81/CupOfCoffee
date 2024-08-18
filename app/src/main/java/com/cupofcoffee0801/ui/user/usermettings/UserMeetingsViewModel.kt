package com.cupofcoffee0801.ui.user.usermettings

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.MeetingRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.cupofcoffee0801.data.worker.DeleteMeetingWorker
import com.cupofcoffee0801.ui.model.MeetingEntry
import com.cupofcoffee0801.ui.model.MeetingsCategory
import com.cupofcoffee0801.ui.model.UserEntry
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject


private const val CATEGORY_TAG = "category"

@HiltViewModel
class UserMeetingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val category = savedStateHandle.get<MeetingsCategory>(CATEGORY_TAG)!!

    private val _dataResult: MutableLiveData<DataResult<UserMeetingsUiState>> =
        MutableLiveData()
    val dataResult: LiveData<DataResult<UserMeetingsUiState>> get() = _dataResult

    var currentJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            initUiState()
        }

        override fun onLost(network: Network) {
            initUiState()
        }
    }

    init {
        networkUtil.registerNetworkCallback(networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    fun initUiState() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val uid = Firebase.auth.uid ?: return@launch
            val user = userRepositoryImpl.getLocalUserByIdInFlow(id = uid)
            user.flatMapLatest { userEntry: UserEntry? ->
                userEntry ?: return@flatMapLatest emptyFlow()
                when (category) {
                    MeetingsCategory.ATTENDED_MEETINGS -> {
                        getMeetingEntries(userEntry.userModel.attendedMeetingIds.keys.toList())
                            .map { value: List<MeetingEntry> ->
                                UserMeetingsUiState(value)
                            }
                    }

                    MeetingsCategory.MADE_MEETINGS ->
                        getMeetingEntries(userEntry.userModel.madeMeetingIds.keys.toList())
                            .map { value: List<MeetingEntry> ->
                                UserMeetingsUiState(value)
                            }
                }
            }.collect {
                _dataResult.postValue(success(it))
            }
        }
    }

    private suspend fun getMeetingEntries(meetingIds: List<String>) =
        meetingRepositoryImpl.getMeetingsByIdsInFlow(meetingIds, networkUtil.isConnected())

    fun getDeleteMeetingWorker(meetingEntry: MeetingEntry): OneTimeWorkRequest {
        val jsonMeetingEntry = Json.encodeToString(meetingEntry)
        val inputData = Data.Builder()
            .putString("meetingEntry", jsonMeetingEntry)
            .build()

        return OneTimeWorkRequestBuilder<DeleteMeetingWorker>()
            .setInputData(inputData)
            .build()
    }
}