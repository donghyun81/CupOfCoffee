package com.cupofcoffee.ui.user.useredit

import android.content.Intent
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cupofcoffee.CupOfCoffeeApplication
import com.cupofcoffee.data.DataResult
import com.cupofcoffee.data.DataResult.Companion.success
import com.cupofcoffee.data.repository.UserRepositoryImpl
import com.cupofcoffee.ui.model.UserEntry
import com.cupofcoffee.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class UserEditViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _uiState: MutableLiveData<DataResult<UserEditUiState>> =
        MutableLiveData(DataResult.loading())
    val uiState: LiveData<DataResult<UserEditUiState>> = _uiState

    init {
        initUiState()
    }

    private fun initUiState() {
        viewModelScope.launch {
            val uid = Firebase.auth.uid!!
            try {
                val user = userRepositoryImpl.getLocalUserById(uid)
                _uiState.value = success(
                    UserEditUiState(
                        userEntry = user,
                        contentUri = user.userModel.profileImageWebUrl
                    )
                )
            } catch (e: Exception) {
                DataResult.error(e)
            }
        }
    }

    fun isNetworkConnected() = networkUtil.isConnected()

    suspend fun updateUiState(contentUri: String?) {
        val uid = Firebase.auth.uid!!
        try {
            val user = userRepositoryImpl.getLocalUserById(uid)
            _uiState.value = success(
                UserEditUiState(
                    userEntry = user,
                    contentUri = contentUri
                )
            )
        } catch (e: Exception) {
            DataResult.error(e)
        }
    }

    suspend fun updateUser(userEntry: UserEntry) {
        userRepositoryImpl.update(userEntry)
    }

    fun getImagePick() = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserEditViewModel(
                    userRepositoryImpl = CupOfCoffeeApplication.userRepository,
                    networkUtil = CupOfCoffeeApplication.networkUtil
                )
            }
        }
    }
}