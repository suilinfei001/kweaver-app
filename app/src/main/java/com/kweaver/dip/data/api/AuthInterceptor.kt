package com.kweaver.dip.data.api

import com.kweaver.dip.data.local.datastore.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking { tokenDataStore.getAccessToken() }
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val cleanToken = token.trim()

        val authenticatedRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $cleanToken")
            .addHeader("Token", cleanToken)
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
