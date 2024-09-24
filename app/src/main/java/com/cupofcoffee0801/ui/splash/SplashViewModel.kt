package com.cupofcoffee0801.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.cupofcoffee0801.util.NetworkUtil
import com.example.data.repository.UserRepository
import com.example.datastore.PreferencesRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    preferencesRepositoryImpl: PreferencesRepositoryImpl,
    private val userRepository: UserRepository,
    private val networkUtil: NetworkUtil
) : ViewModel() {

    val isAutoLoginFlow = preferencesRepositoryImpl.isAutoLoginFlow.asLiveData()

    fun isNetworkConnected() = networkUtil.isConnected()

    suspend fun isUserDeleted(): Boolean {
        val uid = Firebase.auth.uid ?: return true
        val isUserDeleted = userRepository.getUser(uid, isNetworkConnected()) == null
        if (isUserDeleted) Firebase.auth.signOut()
        return isUserDeleted
    }
}