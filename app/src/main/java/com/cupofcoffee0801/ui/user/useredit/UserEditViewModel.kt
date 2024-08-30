package com.cupofcoffee0801.ui.user.useredit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cupofcoffee0801.data.DataResult
import com.cupofcoffee0801.data.DataResult.Companion.success
import com.cupofcoffee0801.data.repository.CommentRepositoryImpl
import com.cupofcoffee0801.data.repository.UserRepositoryImpl
import com.cupofcoffee0801.ui.model.User
import com.cupofcoffee0801.util.NetworkUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserEditViewModel @Inject constructor(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val commentRepositoryImpl: CommentRepositoryImpl,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    private val _dataResult: MutableLiveData<DataResult<UserEditUiState>> =
        MutableLiveData(DataResult.loading())
    val dataResult: LiveData<DataResult<UserEditUiState>> get() = _dataResult

    private val _isButtonClicked: MutableLiveData<Boolean> = MutableLiveData(false)
    val isButtonClicked: LiveData<Boolean> get() = _isButtonClicked


    init {
        initUiState()
    }

    fun onButtonClicked() {
        _isButtonClicked.value = true
    }

    private fun initUiState() {
        viewModelScope.launch {
            val uid = Firebase.auth.uid!!
            try {
                val user = userRepositoryImpl.getLocalUserById(uid)!!
                _dataResult.value = success(
                    UserEditUiState(
                        user = user,
                        contentUri = user.profileImageWebUrl
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
            val user = userRepositoryImpl.getLocalUserById(uid)!!
            _dataResult.value = success(
                UserEditUiState(
                    user = user,
                    contentUri = contentUri
                )
            )
        } catch (e: Exception) {
            DataResult.error(e)
        }
    }

    suspend fun updateUser(user: User) {
        userRepositoryImpl.update(user)
    }

    suspend fun updateUserComments(user: User) {
        commentRepositoryImpl.getCommentsByUserId(user.id).map {
            val (id, commentDTO) = it
            val currentComment =
                commentDTO.copy(profileImageWebUrl = user.profileImageWebUrl)
            commentRepositoryImpl.update(id, currentComment)
        }
    }
}