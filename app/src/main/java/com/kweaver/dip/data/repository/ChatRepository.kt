package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.SseChatService
import com.kweaver.dip.data.local.MessageDao
import com.kweaver.dip.data.local.ConversationDao
import com.kweaver.dip.data.model.AiConfig
import com.kweaver.dip.data.model.ChatMessageDto
import com.kweaver.dip.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val sseChatService: SseChatService,
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
