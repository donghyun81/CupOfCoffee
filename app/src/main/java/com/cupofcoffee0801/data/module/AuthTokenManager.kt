package com.cupofcoffee0801.data.module

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AuthTokenManager {
    @Volatile
    private var uid: String? = null

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uid = task.result?.token
                }
            }
        } else {
            uid = null
        }
    }

    fun initializeAuthListener() {
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    fun removeAuthListener() {
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }

    fun getAuthToken(): String? {
        return uid
    }
}