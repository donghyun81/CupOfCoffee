package com.cupofcoffee.ui.user.usermettings

import android.net.ConnectivityManager
import android.net.Network
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
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.repository.CommentRepositoryImpl
import com.cupofcoffee.data.repository.MeetingRepositoryImpl
import com.cupofcoffee.data.repository.PlaceRepositoryImpl
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.data.worker.DeleteMeetingWorker
import com.cupofcoffee.ui.model.MeetingEntry
import com.cupofcoffee.ui.model.MeetingsCategory
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


private const val CATEGORY_TAG = "category"

class UserMeetingsViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepositoryImpl: UserRepositoryImpl,
    private val meetingRepositoryImpl: MeetingRepositoryImpl,
    private val placeRepositoryImpl: PlaceRepositoryImpl,
    private val commentRepositoryImpl: CommentRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val category = savedStateHandle.get<MeetingsCategory>(CATEGORY_TAG)!!

    private val _uiState: MutableLiveData<DataResult<UserMeetingsUiState>> =
        MutableLiveData()
    val uiState: LiveData<DataResult<UserMeetingsUiState>> get() = _uiState

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
        initUiState()
        networkUtil.registerNetworkCallback(networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    private fun initUiState() =
        viewModelScope.launch {
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
                _uiState.postValue(success(it))
            }
        }

    private suspend fun getMeetingEntries(meetingIds: List<String>) =
        meetingRepositoryImpl.getMeetingsByIdsInFlow(meetingIds, networkUtil.isConnected())

    fun getDeleteMeetingWorker(meetingEntry: MeetingEntry): OneTimeWorkRequest {
        val jsonMeetingEntry = Json.encodeToString(meetingEntry)
        val inputData = Data.Builder()
            .putString("meetingEntry", jsonMeetingEntry)
            .build()

        return OneTimeWorkRequest.Builder(DeleteMeetingWorker::class.java)
            .setInputData(inputData)
            .build()
    }

    private suspend fun updatePlace(placeId: String, meetingId: String) {
        val placeEntry =
            placeRepositoryImpl.getPlaceById(placeId, networkUtil.isConnected()) ?: return
        with(placeEntry) {
            placeModel.meetingIds.remove(meetingId)
            if (placeModel.meetingIds.isEmpty()) {
                placeRepositoryImpl.delete(this)
            } else {
                placeRepositoryImpl.update(placeEntry)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserMeetingsViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    meetingRepositoryImpl = CupOfCoffeeApplication.meetingRepository,
                    placeRepositoryImpl = CupOfCoffeeApplication.placeRepository,
                    commentRepositoryImpl = CupOfCoffeeApplication.commentRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}