package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.api.SseClient
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.CreateSessionKeyRequest
import com.kweaver.dip.data.model.CreateSessionKeyResponse
import com.kweaver.dip.data.model.SseEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class ChatRepositoryTest {

    @Mock
    private lateinit var api: DipStudioApi

    @Mock
    private lateinit var sseClient: SseClient

    @Mock
    private lateinit var tokenDataStore: TokenDataStore

    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = ChatRepository(api, sseClient, tokenDataStore)
    }

    @Test
    fun `createSessionKey returns key on success`() = runTest {
        whenever(tokenDataStore.getUserId()).thenReturn("user1")
        whenever(api.createSessionKey("user1", CreateSessionKeyRequest("agent1")))
            .thenReturn(CreateSessionKeyResponse(sessionKey = "sk-123"))

        val result = repository.createSessionKey("agent1")

        assertTrue(result.isSuccess)
        assertEquals("sk-123", result.getOrNull())
    }

    @Test
    fun `createSessionKey uses default user when userId is null`() = runTest {
        whenever(tokenDataStore.getUserId()).thenReturn(null)
        whenever(api.createSessionKey("default-user", CreateSessionKeyRequest("agent1")))
            .thenReturn(CreateSessionKeyResponse(sessionKey = "sk-default"))

        val result = repository.createSessionKey("agent1")

        assertTrue(result.isSuccess)
        assertEquals("sk-default", result.getOrNull())
    }

    @Test
    fun `createSessionKey returns failure on exception`() = runTest {
        whenever(tokenDataStore.getUserId()).thenReturn("user1")
        whenever(api.createSessionKey("user1", CreateSessionKeyRequest("agent1")))
            .thenThrow(RuntimeException("Network error"))

        val result = repository.createSessionKey("agent1")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `streamChat returns flow of SSE events`() = runTest {
        val events = listOf(
            SseEvent(event = "message", data = "Hello"),
            SseEvent(event = "done", data = "")
        )
        whenever(tokenDataStore.getServerUrl()).thenReturn("https://example.com")
        whenever(tokenDataStore.getUserId()).thenReturn("user1")
        whenever(sseClient.streamChat("https://example.com", "sk-123", "user1", "Hi", null))
            .thenReturn(flowOf(*events.toTypedArray()))

        val flow = repository.streamChat("sk-123", "Hi")
        val firstEvent = flow.first()

        assertEquals("message", firstEvent.event)
        assertEquals("Hello", firstEvent.data)
    }

    @Test
    fun `uploadFile returns result on success`() = runTest {
        val uploadResult = com.kweaver.dip.data.model.ChatUploadResult(name = "test.pdf", path = "/uploads/test.pdf")
        whenever(api.uploadChatFile(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(uploadResult)

        val result = repository.uploadFile("sk-123", "test.pdf", byteArrayOf(1, 2, 3))

        assertTrue(result.isSuccess)
        assertEquals("test.pdf", result.getOrNull()!!.name)
    }

    @Test
    fun `uploadFile returns failure on exception`() = runTest {
        whenever(api.uploadChatFile(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenThrow(RuntimeException("Upload failed"))

        val result = repository.uploadFile("sk-123", "test.pdf", byteArrayOf())

        assertTrue(result.isFailure)
    }
}
