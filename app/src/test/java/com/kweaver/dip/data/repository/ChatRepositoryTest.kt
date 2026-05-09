package com.kweaver.dip.data.repository

import com.google.gson.Gson
import com.kweaver.dip.data.api.SseChatService
import com.kweaver.dip.data.local.AiConfigDataStore
import com.kweaver.dip.data.local.ConversationDao
import com.kweaver.dip.data.local.MessageDao
import com.kweaver.dip.data.model.AiConfig
import com.kweaver.dip.data.model.ChatMessageDto
import com.kweaver.dip.data.model.MessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ChatRepositoryTest {

    private lateinit var messageDao: MessageDao
    private lateinit var conversationDao: ConversationDao
    private lateinit var sseService: SseChatService
    private lateinit var repository: ChatRepository

    private val testConfig = AiConfig(
        baseUrl = "http://localhost",
        modelId = "test-model",
        apiKey = "test-key",
        contextSize = 4096,
    )

    @Before
    fun setup() {
        messageDao = mock()
        conversationDao = mock()
        sseService = mock()
        repository = ChatRepository(messageDao, conversationDao, sseService)
    }

    @Test
    fun sendMessageInsertsUserMessageAndReturnsFlow() = runTest {
        whenever(messageDao.getRecentByConversation(any(), any())).thenReturn(emptyList())
        whenever(sseService.streamChat(any(), any(), any(), any(), any(), any())).thenReturn(flowOf("Hello"))

        val flow = repository.sendMessage(testConfig, conversationId = 1, userContent = "Hi")
        val chunks = flow.first()

        verify(messageDao).insert(argThat { role == "user" && content == "Hi" })
        verify(conversationDao).updateTimestamp(eq(1), any())
        assertEquals("Hello", chunks)
    }

    @Test
    fun saveAssistantMessageInsertsAndReturnsId() = runTest {
        whenever(messageDao.insert(any())).thenReturn(42L)

        val id = repository.saveAssistantMessage(conversationId = 1, content = "Response text")

        assertEquals(42L, id)
        verify(messageDao).insert(argThat { role == "assistant" && content == "Response text" })
    }

    @Test
    fun updateMessageContentAndStatusDelegatesToDao() = runTest {
        repository.updateMessageContentAndStatus(10L, "updated", "sent")

        verify(messageDao).updateContentAndStatus(10L, "updated", "sent")
    }

    @Test
    fun sendMessagePassesRecentMessagesToSseService() = runTest {
        val recentMessages = listOf(
            MessageEntity(id = 1, conversationId = 1, role = "user", content = "First"),
            MessageEntity(id = 2, conversationId = 1, role = "assistant", content = "Reply"),
        )
        whenever(messageDao.getRecentByConversation(any(), any())).thenReturn(recentMessages)
        whenever(sseService.streamChat(any(), any(), any(), any(), any(), any())).thenReturn(flowOf("Ok"))

        repository.sendMessage(testConfig, conversationId = 1, userContent = "Second").first()

        verify(sseService).streamChat(
            eq("http://localhost"),
            eq("test-key"),
            eq("test-model"),
            argThat { size == 2 && this[0].content == "First" && this[1].content == "Reply" },
            eq(4096),
            any(),
        )
    }
}
