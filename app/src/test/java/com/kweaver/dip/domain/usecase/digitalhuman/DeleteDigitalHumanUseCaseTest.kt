package com.kweaver.dip.domain.usecase.digitalhuman

import com.kweaver.dip.data.repository.DigitalHumanRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DeleteDigitalHumanUseCaseTest {

    @Mock
    private lateinit var repository: DigitalHumanRepository

    private lateinit var useCase: DeleteDigitalHumanUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = DeleteDigitalHumanUseCase(repository)
    }

    @Test
    fun `invoke deletes digital human`() = runTest {
        whenever(repository.deleteDigitalHuman("1", false)).thenReturn(Result.success(Unit))

        val result = useCase("1")

        assertTrue(result.isSuccess)
        verify(repository).deleteDigitalHuman("1", false)
    }

    @Test
    fun `invoke deletes with files`() = runTest {
        whenever(repository.deleteDigitalHuman("1", true)).thenReturn(Result.success(Unit))

        val result = useCase("1", deleteFiles = true)

        assertTrue(result.isSuccess)
        verify(repository).deleteDigitalHuman("1", true)
    }

    @Test
    fun `invoke returns failure on error`() = runTest {
        whenever(repository.deleteDigitalHuman("1", false)).thenReturn(Result.failure(RuntimeException("Error")))

        val result = useCase("1")

        assertTrue(result.isFailure)
    }
}
