package com.kweaver.dip.data.repository

import com.google.gson.Gson
import com.kweaver.dip.data.api.SseChatService
import com.kweaver.dip.data.local.MessageDao
import com.kweaver.dip.data.local.ConversationDao
import com.kweaver.dip.data.model.AiConfig
import com.kweaver.dip.data.model.ChatMessageDto
import com.kweaver.dip.data.model.ChatRequest
import com.kweaver.dip.data.model.ChatResponse
import com.kweaver.dip.data.model.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val sseChatService: SseChatService,
    private val client: OkHttpClient,
    private val gson: Gson,
) {
    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        messageDao.getByConversation(conversationId)

    suspend fun sendMessage(
        config: AiConfig,
        conversationId: Long,
        userContent: String,
        messageLimit: Int = 20,
    ): Flow<String> {
        val userMsg = MessageEntity(
            conversationId = conversationId,
            role = "user",
            content = userContent,
            status = "sent",
        )
        messageDao.insert(userMsg)

        val recent = messageDao.getRecentByConversation(conversationId, limit = messageLimit)
        val dtoMessages = recent.map { ChatMessageDto(it.role, it.content) }

        conversationDao.updateTimestamp(conversationId, System.currentTimeMillis())

        return sseChatService.streamChat(
            baseUrl = config.baseUrl,
            apiKey = config.apiKey,
            model = config.modelId,
            messages = dtoMessages,
            contextSize = config.contextSize,
        )
    }

    suspend fun sendNonStreaming(
        config: AiConfig,
        conversationId: Long,
        userContent: String,
        messageLimit: Int = 20,
    ): String {
        val recent = messageDao.getRecentByConversation(conversationId, limit = messageLimit)
        val dtoMessages = recent.map { ChatMessageDto(it.role, it.content) }

        return withContext(Dispatchers.IO) {
            val requestBody = gson.toJson(
                ChatRequest(model = config.modelId, messages = dtoMessages, stream = false)
            ).toRequestBody("application/json".toMediaType())

            val requestBuilder = Request.Builder()
                .url("${config.baseUrl.trimEnd('/')}/chat/completions")
                .header("Content-Type", "application/json")
                .post(requestBody)
            if (config.apiKey.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer ${config.apiKey}")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: $body")
            }

            val chatResponse = gson.fromJson(body, ChatResponse::class.java)
            chatResponse.choices.firstOrNull()?.message?.content ?: ""
        }
    }

    suspend fun saveAssistantMessage(
        conversationId: Long,
        content: String,
        status: String = "sent",
    ): Long {
        return messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                role = "assistant",
                content = content,
                status = status,
            )
        )
    }

    suspend fun updateMessageContentAndStatus(id: Long, content: String, status: String) {
        messageDao.updateContentAndStatus(id, content, status)
    }
}
