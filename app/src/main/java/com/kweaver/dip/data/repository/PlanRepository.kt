package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.*
import javax.inject.Inject

class PlanRepository @Inject constructor(
    private val api: DipStudioApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun listPlans(): Result<List<CronJob>> = try {
        Result.success(api.listPlans())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPlan(id: String): Result<CronJob> = try {
        Result.success(api.getPlan(id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPlanContent(id: String): Result<String> = try {
        val result = api.getPlanContent(id)
        Result.success(result.content)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun listPlanRuns(id: String): Result<List<CronRunEntry>> = try {
        Result.success(api.listPlanRuns(id, limit = 20))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePlan(id: String, request: UpdatePlanRequest): Result<CronJob> = try {
        Result.success(api.updatePlan(id, request))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePlan(id: String): Result<Unit> = try {
        val response = api.deletePlan(id)
        if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Delete failed"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun listDigitalHumanPlans(agentId: String): Result<List<CronJob>> = try {
        Result.success(api.listDigitalHumanPlans(agentId))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
