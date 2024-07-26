package com.cupofcoffee.data.module

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object AuthTokenManager {
    @Volatile
    private var uid: String? = null

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            // 사용자 로그인 시 토큰 업데이트
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    uid = task.result?.token
                }
            }
        } else {
            // 사용자 로그아웃 시 토큰을 null로 설정
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
        Log.d("auth", uid.toString())
        return uid
    }
}