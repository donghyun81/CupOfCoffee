package com.cupofcoffee.data.module

import com.google.firebase.auth.FirebaseAuth

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