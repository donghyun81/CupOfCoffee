package com.cupofcoffee0801.data.repository

import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PreferencesRepository {

    val isAutoLoginFlow: Flow<Boolean>

    suspend fun toggleAutoLogin()
}