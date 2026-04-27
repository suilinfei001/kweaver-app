package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.model.GuideStatus
import com.kweaver.dip.data.model.InitializeGuideRequest
import com.kweaver.dip.data.model.OpenClawConfig
import javax.inject.Inject

class GuideRepository @Inject constructor(
    private val api: DipStudioApi
) {
    suspend fun getStatus(): Result<GuideStatus> = try {
        Result.success(api.getGuideStatus())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getOpenClawConfig(): Result<OpenClawConfig> = try {
        Result.success(api.getOpenClawConfig())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun initialize(request: InitializeGuideRequest): Result<Unit> = try {
        val response = api.initializeGuide(request)
        if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Initialize failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
