package com.kweaver.dip.data.repository

import android.util.Log
import com.kweaver.dip.data.api.DipHubApi
import com.kweaver.dip.data.local.datastore.TokenDataStore
import okhttp3.Headers
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val dipHubApi: DipHubApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun login(serverUrl: String, username: String, password: String): Result<String> {
        return try {
            tokenDataStore.saveServerUrl(serverUrl)
            val response = dipHubApi.login(username, password)
            if (response.isSuccessful) {
                val cookies = response.headers().values("Set-Cookie")
                var accessToken: String? = null
                var refreshToken: String? = null

                for (cookie in cookies) {
                    if (cookie.startsWith("dip.oauth2_token=")) {
                        accessToken = cookie.substringAfter("dip.oauth2_token=").substringBefore(";")
                    }
                    if (cookie.startsWith("dip.refresh_token=")) {
                        refreshToken = cookie.substringAfter("dip.refresh_token=").substringBefore(";")
                    }
                }

                val bodyToken = response.body()?.string()
                if (accessToken.isNullOrBlank() && bodyToken != null) {
                    val gson = com.google.gson.Gson()
                    val map = gson.fromJson(bodyToken, Map::class.java) as? Map<*, *>
                    accessToken = map?.get("access_token") as? String
                    refreshToken = refreshToken ?: map?.get("refresh_token") as? String
                }

                if (!accessToken.isNullOrBlank()) {
                    tokenDataStore.saveTokens(accessToken, refreshToken)
                    tokenDataStore.saveUsername(username)
                    val userId = parseUserId(accessToken)
                    if (userId != null) {
                        tokenDataStore.saveUserId(userId)
                    }
                    Result.success(accessToken)
                } else {
                    Result.failure(Exception("No token received from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Login failed: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error", e)
            Result.failure(e)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return !tokenDataStore.getAccessToken().isNullOrBlank()
    }

    suspend fun logout() {
        tokenDataStore.clearAll()
    }

    private fun parseUserId(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = android.util.Base64.decode(
                    parts[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING
                )
                val json = String(payload)
                val gson = com.google.gson.Gson()
                val map = gson.fromJson(json, Map::class.java) as? Map<*, *>
                (map?.get("sub") as? String) ?: (map?.get("user_id") as? String)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
