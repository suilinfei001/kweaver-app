package com.kweaver.dip.data.api

import com.kweaver.dip.data.model.ApplicationInfo
import com.kweaver.dip.data.model.PinAppRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DipHubApi {

    @GET("api/dip-hub/v1/login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<ResponseBody>

    @GET("api/dip-hub/v1/refresh-token")
    suspend fun refreshToken(): Response<ResponseBody>

    @GET("api/dip-hub/v1/applications")
    suspend fun listApplications(): List<ApplicationInfo>

    @POST("api/dip-hub/v1/applications")
    suspend fun installApplication(
        @Body body: RequestBody
    ): ApplicationInfo

    @DELETE("api/dip-hub/v1/applications/{key}")
    suspend fun uninstallApplication(
        @Path("key") key: String
    ): Response<Unit>

    @PUT("api/dip-hub/v1/applications/{key}/pinned")
    suspend fun pinApplication(
        @Path("key") key: String,
        @Body request: PinAppRequest
    ): ApplicationInfo
}
