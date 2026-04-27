package com.kweaver.dip.di

import com.kweaver.dip.data.api.AuthInterceptor
import com.kweaver.dip.data.api.DipHubApi
import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.api.TokenAuthenticator
import com.kweaver.dip.data.local.datastore.TokenDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, tokenDataStore: TokenDataStore): Retrofit {
        val serverUrl = runBlocking { tokenDataStore.getServerUrl() }
        return Retrofit.Builder()
            .baseUrl("$serverUrl/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDipStudioApi(retrofit: Retrofit): DipStudioApi =
        retrofit.create(DipStudioApi::class.java)

    @Provides
    @Singleton
    fun provideDipHubApi(retrofit: Retrofit): DipHubApi =
        retrofit.create(DipHubApi::class.java)

    private fun runBlocking(block: suspend () -> String): String =
        kotlinx.coroutines.runBlocking { block() }
}
