package com.kweaver.dip.ui.screens.chat

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kweaver.dip.data.model.SseEvent
import com.kweaver.dip.domain.usecase.chat.CreateSessionUseCase
import com.kweaver.dip.domain.usecase.chat.SendMessageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class ChatViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var createSessionUseCase: CreateSessionUseCase

    @Mock
    private lateinit var sendMessageUseCase: SendMessageUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initSession with existing key sets sessionKey`() = runTest {
        val viewModel = ChatViewModel(createSessionUseCase, sendMessageUseCase)
        viewModel.initSession("agent1", "existing-key")
        advanceUntilIdle()

        assertEquals("existing-key", viewModel.uiState.value.sessionKey)
        assertFalse(viewModel.uiState.value.isCreatingSession)
    }

    @Test
    fun `initSession creates new session key`() = runTest {
        whenever(createSessionUseCase("agent1"))
            .thenReturn(Result.success("new-key"))

        val viewModel = ChatViewModel(createSessionUseCase, sendMessageUseCase)
        viewModel.initSession("agent1", null)
        advanceUntilIdle()

        assertEquals("new-key", viewModel.uiState.value.sessionKey)
        assertFalse(viewModel.uiState.value.isCreatingSession)
    }

    @Test
    fun `initSession handles error`() = runTest {
        whenever(createSessionUseCase("agent1"))
            .thenReturn(Result.failure(Exception("No connection")))

        val viewModel = ChatViewModel(createSessionUseCase, sendMessageUseCase)
        viewModel.initSession("agent1", null)
        advanceUntilIdle()

        assertEquals("No connection", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isCreatingSession)
    }

    @Test
    fun `sendMessage with blank text does nothing`() = runTest {
        val viewModel = ChatViewModel(createSessionUseCase, sendMessageUseCase)
        viewModel.initSession("agent1", "key1")
        viewModel.sendMessage("")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.isEmpty())
    }

    @Test
    fun `sendMessage without sessionKey does nothing`() = runTest {
        val viewModel = ChatViewModel(createSessionUseCase, sendMessageUseCase)
        viewModel.sendMessage("hello")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.messages.isEmpty())
    }

    @Test
    fun `sendMessage adds user message and streams response`() = runTest {
        whenever(sendMessageUseCase("key1", "hello", null))
            .thenReturn(flowOf(
                SseEvent("message", "world"),
                SseEvent("done", "")
            ))

        val viewModel = ChatViewModel(createSessionUseCase, sendMessageUseCase)
        viewModel.initSession("agent1", "key1")
        advanceUntilIdle()

        viewModel.sendMessage("hello")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.messages.size)
        assertEquals("hello", viewModel.uiState.value.messages[0].content)
        assertEquals("world", viewModel.uiState.value.messages[1].content)
        assertFalse(viewModel.uiState.value.isStreaming)
        assertFalse(viewModel.uiState.value.isSending)
    }

    @Test
    fun `sendMessage handles error event`() = runTest {
        whenever(sendMessageUseCase("key1", "hello", null))
            .thenReturn(flowOf(SseEvent("error", "Rate limited")))

        val viewModel = ChatViewModel(createSessionUseCase, sendMessageUseCase)
        viewModel.initSession("agent1", "key1")
        advanceUntilIdle()

        viewModel.sendMessage("hello")
        advanceUntilIdle()

        assertEquals("Rate limited", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isStreaming)
    }
}
