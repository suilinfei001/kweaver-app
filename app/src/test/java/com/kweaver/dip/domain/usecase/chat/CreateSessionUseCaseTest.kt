package com.kweaver.dip.domain.usecase.chat

import com.kweaver.dip.data.repository.ChatRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class CreateSessionUseCaseTest {

    @Mock
    private lateinit var chatRepository: ChatRepository

    private lateinit var createSessionUseCase: CreateSessionUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        createSessionUseCase = CreateSessionUseCase(chatRepository)
    }

    @Test
    fun `invoke returns session key on success`() = runTest {
        whenever(chatRepository.createSessionKey("agent1"))
            .thenReturn(Result.success("sk-123"))

        val result = createSessionUseCase("agent1")

        assertTrue(result.isSuccess)
        assertEquals("sk-123", result.getOrNull())
    }

    @Test
    fun `invoke returns failure on repository error`() = runTest {
        whenever(chatRepository.createSessionKey("agent1"))
            .thenReturn(Result.failure(RuntimeException("Network error")))

        val result = createSessionUseCase("agent1")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure on empty agentId`() = runTest {
        whenever(chatRepository.createSessionKey(""))
            .thenReturn(Result.failure(RuntimeException("Agent ID required")))

        val result = createSessionUseCase("")

        assertTrue(result.isFailure)
    }
}
