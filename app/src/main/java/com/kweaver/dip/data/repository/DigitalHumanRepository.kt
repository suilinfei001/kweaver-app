package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.*
import javax.inject.Inject

class DigitalHumanRepository @Inject constructor(
    private val api: DipStudioApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun listDigitalHumans(): Result<List<DigitalHuman>> = try {
        Result.success(api.listDigitalHumans())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDigitalHuman(id: String): Result<DigitalHumanDetail> = try {
        Result.success(api.getDigitalHuman(id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createDigitalHuman(request: CreateDigitalHumanRequest): Result<CreateDigitalHumanResult> = try {
        Result.success(api.createDigitalHuman(request))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateDigitalHuman(id: String, request: UpdateDigitalHumanRequest): Result<DigitalHumanDetail> = try {
        Result.success(api.updateDigitalHuman(id, request))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteDigitalHuman(id: String, deleteFiles: Boolean = false): Result<Unit> = try {
        val response = api.deleteDigitalHuman(id, deleteFiles)
        if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Delete failed: ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun listBuiltIn(): Result<List<BuiltInDigitalHuman>> = try {
        Result.success(api.listBuiltInDigitalHumans())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createBuiltIn(ids: String): Result<List<CreateDigitalHumanResult>> = try {
        Result.success(api.createBuiltInDigitalHumans(ids))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
