package com.kweaver.dip.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "kweaver_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            if (refreshToken != null) {
                prefs[KEY_REFRESH_TOKEN] = refreshToken
            }
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first()

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { it[KEY_SERVER_URL] = url.trimEnd('/') }
    }

    suspend fun getServerUrl(): String =
        context.dataStore.data.map { it[KEY_SERVER_URL] ?: "https://192.168.40.110" }.first()

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { it[KEY_USER_ID] = userId }
    }

    suspend fun getUserId(): String? =
        context.dataStore.data.map { it[KEY_USER_ID] }.first()

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { it[KEY_USERNAME] = username }
    }

    suspend fun getUsername(): String? =
        context.dataStore.data.map { it[KEY_USERNAME] }.first()

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
