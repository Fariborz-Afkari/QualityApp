package com.example.qualityapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_NAME   = stringPreferencesKey("personnel_name")
    private val KEY_NUMBER = stringPreferencesKey("personnel_number")
    private val KEY_TOKEN  = stringPreferencesKey("github_token")

    val personnelName: Flow<String>   = context.dataStore.data.map { it[KEY_NAME]   ?: "" }
    val personnelNumber: Flow<String> = context.dataStore.data.map { it[KEY_NUMBER] ?: "" }
    val githubToken: Flow<String>     = context.dataStore.data.map { it[KEY_TOKEN]  ?: "" }

    suspend fun currentName(): String   = personnelName.first()
    suspend fun currentNumber(): String = personnelNumber.first()
    suspend fun currentToken(): String  = githubToken.first()

    suspend fun savePersonnelInfo(name: String, number: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME]   = name.trim()
            prefs[KEY_NUMBER] = number.trim()
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_TOKEN] = token.trim() }
    }
}