package com.example.datastore

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val isAutoLoginFlow: Flow<Boolean>

    suspend fun toggleAutoLogin()
}