package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.api.SseClient
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val api: DipStudioApi,
    private val sseClient: SseClient,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun createSessionKey(agentId: String): Result<String> = try {
        val userId = tokenDataStore.getUserId() ?: "default-user"
        val response = api.createSessionKey(userId, CreateSessionKeyRequest(agentId))
        Result.success(response.sessionKey)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun streamChat(
        sessionKey: String,
        message: String,
        attachments: List<Pair<String, String>>? = null
    ): Flow<SseEvent> {
        val serverUrl = kotlinx.coroutines.runBlocking { tokenDataStore.getServerUrl() }
        val userId = kotlinx.coroutines.runBlocking { tokenDataStore.getUserId() } ?: "default-user"
        return sseClient.streamChat(serverUrl, sessionKey, userId, message, attachments)
    }

    suspend fun uploadFile(sessionKey: String, fileName: String, fileBytes: ByteArray): Result<ChatUploadResult> = try {
        val requestBody = okhttp3.RequestBody.create(
            "application/octet-stream".toMediaType(), fileBytes
        )
        val multipartBody = okhttp3.MultipartBody.Part.createFormData("file", fileName, requestBody)
        Result.success(api.uploadChatFile(sessionKey, multipartBody))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
