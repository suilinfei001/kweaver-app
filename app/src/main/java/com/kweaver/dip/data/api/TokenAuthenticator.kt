package com.kweaver.dip.data.api

import com.kweaver.dip.data.local.datastore.TokenDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Authenticator {

    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null

        return runBlocking {
            refreshMutex.withLock {
                val currentToken = tokenDataStore.getAccessToken()
                val failedToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")

                if (currentToken != failedToken) {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .header("Token", currentToken ?: "")
                        .build()
                }

                val serverUrl = tokenDataStore.getServerUrl()
                val refreshToken = tokenDataStore.getRefreshToken()

                if (refreshToken.isNullOrBlank()) {
                    tokenDataStore.clearAll()
                    return@runBlocking null
                }

                try {
                    val refreshResponse = okhttp3.OkHttpClient().newCall(
                        okhttp3.Request.Builder()
                            .url("$serverUrl/api/dip-hub/v1/refresh-token")
                            .addHeader("Authorization", "Bearer $refreshToken")
                            .get()
                            .build()
                    ).execute()

                    if (refreshResponse.isSuccessful) {
                        val body = refreshResponse.body?.string()
                        val newToken = parseAccessToken(body)
                        if (newToken != null) {
                            tokenDataStore.saveTokens(newToken, null)
                            return@runBlocking response.request.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .header("Token", newToken)
                                .build()
                        }
                    }

                    tokenDataStore.clearAll()
                    null
                } catch (e: Exception) {
                    tokenDataStore.clearAll()
                    null
                }
            }
        }
    }

    private fun parseAccessToken(body: String?): String? {
        if (body.isNullOrBlank()) return null
        val gson = com.google.gson.Gson()
        val map = gson.fromJson(body, Map::class.java) as? Map<*, *> ?: return null
        return map["access_token"] as? String
    }
}
