package com.kweaver.dip.data.repository

import android.util.Log
import com.kweaver.dip.data.api.BaseUrlInterceptor
import com.kweaver.dip.data.api.OAuth2LoginHelper
import com.kweaver.dip.data.local.datastore.TokenDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val oAuth2LoginHelper: OAuth2LoginHelper,
    private val tokenDataStore: TokenDataStore,
    private val baseUrlInterceptor: BaseUrlInterceptor
) {
    suspend fun login(serverUrl: String, username: String, password: String): Result<String> {
        return try {
            tokenDataStore.saveServerUrl(serverUrl)
            baseUrlInterceptor.updateBaseUrl(serverUrl)
            val result = withContext(Dispatchers.IO) {
                oAuth2LoginHelper.login(serverUrl, username, password)
            }
            tokenDataStore.saveTokens(result.accessToken, result.refreshToken)
            tokenDataStore.saveUsername(username)
            val userId = parseUserId(result.accessToken)
            if (userId != null) {
                tokenDataStore.saveUserId(userId)
            }
            Result.success(result.accessToken)
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
