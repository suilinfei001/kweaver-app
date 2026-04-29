package com.kweaver.dip.domain.usecase.chat

import com.kweaver.dip.data.model.SseEvent
import com.kweaver.dip.data.repository.ChatRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class SendMessageUseCaseTest {

    @Mock
    private lateinit var chatRepository: ChatRepository

    private lateinit var sendMessageUseCase: SendMessageUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        sendMessageUseCase = SendMessageUseCase(chatRepository)
    }

    @Test
    fun `invoke returns flow of SSE events`() = runTest {
        val events = listOf(
            SseEvent(event = "message", data = "Hello "),
            SseEvent(event = "message", data = "world"),
            SseEvent(event = "done", data = "")
        )
        whenever(chatRepository.streamChat("sk-123", "Hi", null))
            .thenReturn(flowOf(*events.toTypedArray()))

        val flow = sendMessageUseCase("sk-123", "Hi")
        val firstEvent = flow.first()

        assertEquals("message", firstEvent.event)
        assertEquals("Hello ", firstEvent.data)
    }

    @Test
    fun `invoke passes attachments to repository`() = runTest {
        val attachments = listOf("file1.pdf" to "/path/file1.pdf")
        val events = listOf(SseEvent(event = "done", data = ""))
        whenever(chatRepository.streamChat("sk-123", "Check this", attachments))
            .thenReturn(flowOf(*events.toTypedArray()))

        val flow = sendMessageUseCase("sk-123", "Check this", attachments)
        val event = flow.first()

        assertEquals("done", event.event)
    }

    @Test
    fun `invoke handles error events`() = runTest {
        val errorEvent = SseEvent(event = "error", data = "Something went wrong")
        whenever(chatRepository.streamChat("sk-123", "Hi", null))
            .thenReturn(flowOf(errorEvent))

        val flow = sendMessageUseCase("sk-123", "Hi")
        val event = flow.first()

        assertEquals("error", event.event)
        assertEquals("Something went wrong", event.data)
    }
}
