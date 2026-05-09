package com.kweaver.dip.data.api

import com.kweaver.dip.data.model.ChatRequest
import com.kweaver.dip.data.model.ChatResponse
import com.kweaver.dip.data.model.ModelsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ChatApiService {
    @GET("models")
    suspend fun getModels(@Header("Authorization") auth: String): ModelsResponse

    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") auth: String,
        @Body request: ChatRequest,
    ): ChatResponse
}
