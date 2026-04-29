package com.kweaver.dip.domain.usecase.session

import com.kweaver.dip.data.model.Session
import com.kweaver.dip.data.repository.SessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class ListSessionsUseCaseTest {

    @Mock
    private lateinit var repository: SessionRepository

    private lateinit var useCase: ListSessionsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ListSessionsUseCase(repository)
    }

    @Test
    fun `invoke returns sessions without filters`() = runTest {
        val expected = listOf(Session(key = "s1", label = "Chat 1"))
        whenever(repository.listSessions(null, null)).thenReturn(Result.success(expected))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke filters by search`() = runTest {
        val expected = listOf(Session(key = "s1", label = "AI Chat"))
        whenever(repository.listSessions("AI", null)).thenReturn(Result.success(expected))

        val result = useCase(search = "AI")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke filters by agentId`() = runTest {
        whenever(repository.listSessions(null, "a1")).thenReturn(Result.success(emptyList()))

        val result = useCase(agentId = "a1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure on error`() = runTest {
        whenever(repository.listSessions(null, null)).thenReturn(Result.failure(RuntimeException("Error")))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
