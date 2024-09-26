package com.example.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(@ApplicationContext context: Context) :
    PreferencesRepository {
    private val dataStore = context.dataStore


    private val IS_AUTO_LOGIN = booleanPreferencesKey("is_auto_login")

    override val isAutoLoginFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_AUTO_LOGIN] ?: true
    }

    override suspend fun toggleAutoLogin() {
        dataStore.edit { preferences ->
            val current = preferences[IS_AUTO_LOGIN] ?: true
            preferences[IS_AUTO_LOGIN] = !current
        }
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")