package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.Session
import com.kweaver.dip.data.model.SessionMessage
import com.kweaver.dip.data.model.SessionSummary
import javax.inject.Inject

class SessionRepository @Inject constructor(
    private val api: DipStudioApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun listSessions(search: String? = null, agentId: String? = null): Result<List<Session>> = try {
        Result.success(api.listSessions(limit = 50, search = search, agentId = agentId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSessionSummary(key: String): Result<SessionSummary> = try {
        Result.success(api.getSessionSummary(key))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSessionMessages(key: String): Result<List<SessionMessage>> = try {
        Result.success(api.getSessionMessages(key))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteSession(key: String): Result<Unit> = try {
        val response = api.deleteSession(key)
        if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Delete failed"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun listDigitalHumanSessions(agentId: String): Result<List<Session>> = try {
        Result.success(api.listDigitalHumanSessions(agentId, limit = 20))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
