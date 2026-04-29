package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipHubApi
import com.kweaver.dip.data.model.ApplicationInfo
import com.kweaver.dip.data.model.PinAppRequest
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val api: DipHubApi
) {
    suspend fun listApplications(): Result<List<ApplicationInfo>> = try {
        Result.success(api.listApplications())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uninstallApplication(key: String): Result<Unit> = try {
        val response = api.uninstallApplication(key)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Uninstall failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun pinApplication(key: String, pinned: Boolean): Result<ApplicationInfo> = try {
        Result.success(api.pinApplication(key, PinAppRequest(pinned = pinned)))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
