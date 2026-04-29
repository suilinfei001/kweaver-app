package com.kweaver.dip.domain.usecase.store

import com.kweaver.dip.data.model.ApplicationInfo
import com.kweaver.dip.data.repository.AppRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class ListAppsUseCaseTest {

    @Mock
    private lateinit var repository: AppRepository

    private lateinit var useCase: ListAppsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ListAppsUseCase(repository)
    }

    @Test
    fun `invoke returns apps on success`() = runTest {
        val expected = listOf(
            ApplicationInfo(id = 1, key = "app1", name = "App 1"),
            ApplicationInfo(id = 2, key = "app2", name = "App 2")
        )
        whenever(repository.listApplications()).thenReturn(Result.success(expected))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke returns empty list`() = runTest {
        whenever(repository.listApplications()).thenReturn(Result.success(emptyList()))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke returns failure on error`() = runTest {
        whenever(repository.listApplications()).thenReturn(Result.failure(RuntimeException("Error")))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
