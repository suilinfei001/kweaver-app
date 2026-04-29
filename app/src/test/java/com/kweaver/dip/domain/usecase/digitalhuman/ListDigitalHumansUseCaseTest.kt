package com.kweaver.dip.domain.usecase.digitalhuman

import com.kweaver.dip.data.model.DigitalHuman
import com.kweaver.dip.data.repository.DigitalHumanRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class ListDigitalHumansUseCaseTest {

    @Mock
    private lateinit var repository: DigitalHumanRepository

    private lateinit var useCase: ListDigitalHumansUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ListDigitalHumansUseCase(repository)
    }

    @Test
    fun `invoke returns list on success`() = runTest {
        val expected = listOf(DigitalHuman(id = "1", name = "Agent A"))
        whenever(repository.listDigitalHumans()).thenReturn(Result.success(expected))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke returns empty list`() = runTest {
        whenever(repository.listDigitalHumans()).thenReturn(Result.success(emptyList()))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke returns failure on error`() = runTest {
        whenever(repository.listDigitalHumans()).thenReturn(Result.failure(RuntimeException("Error")))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
