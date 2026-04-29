package com.kweaver.dip.data.api

import com.kweaver.dip.data.local.datastore.TokenDataStore
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaseUrlInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {

    private var cachedUrl: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val storedUrl = cachedUrl ?: kotlinx.coroutines.runBlocking {
            tokenDataStore.getServerUrl()
        }.also { cachedUrl = it }

        val target = storedUrl.trimEnd('/').toHttpUrl()
        val newUrl = originalRequest.url.newBuilder()
            .scheme(target.scheme)
            .host(target.host)
            .port(target.port)
            .build()

        return chain.proceed(originalRequest.newBuilder().url(newUrl).build())
    }

    fun updateBaseUrl(url: String) {
        cachedUrl = url.trimEnd('/')
    }
}
