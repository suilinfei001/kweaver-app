package com.kweaver.dip.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface DipHubApi {

    @GET("api/dip-hub/v1/login")
    suspend fun login(
        @retrofit2.http.Query("username") username: String,
        @retrofit2.http.Query("password") password: String
    ): Response<ResponseBody>

    @GET("api/dip-hub/v1/refresh-token")
    suspend fun refreshToken(): Response<ResponseBody>
}
