package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "were_here_prefs")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        val SELECTED_GOAL_ID = longPreferencesKey("selected_goal_id")
        val AI_MODE = stringPreferencesKey("ai_mode") // HYBRID, GEMINI, LOCAL
        val THEME_MODE = stringPreferencesKey("theme_mode") // SYSTEM, DARK, LIGHT
        val SHOW_COMPLETED_TASKS = booleanPreferencesKey("show_completed_tasks")
        val CUSTOM_BACKEND_URL = stringPreferencesKey("custom_backend_url")
        val DEV_API_KEY = stringPreferencesKey("dev_api_key")
        val AI_CONNECTION_STATUS = stringPreferencesKey("ai_connection_status")
        val LAST_AI_TEST_TIME = longPreferencesKey("last_ai_test_time")
    }

    val selectedGoalId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_GOAL_ID]
    }

    val aiMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[AI_MODE] ?: "HYBRID"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "DARK"
    }

    val customBackendUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_BACKEND_URL] ?: ""
    }

    val devApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DEV_API_KEY] ?: ""
    }

    val aiConnectionStatus: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[AI_CONNECTION_STATUS] ?: "OFFLINE_FALLBACK"
    }

    val lastAiTestTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_AI_TEST_TIME] ?: 0L
    }

    suspend fun setSelectedGoalId(goalId: Long) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_GOAL_ID] = goalId
        }
    }

    suspend fun setAiMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[AI_MODE] = mode
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    suspend fun setCustomBackendUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[CUSTOM_BACKEND_URL] = url
        }
    }

    suspend fun setDevApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[DEV_API_KEY] = key
        }
    }

    suspend fun setAiConnectionStatus(status: String, timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[AI_CONNECTION_STATUS] = status
            prefs[LAST_AI_TEST_TIME] = timestamp
        }
    }
}
