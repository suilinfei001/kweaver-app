package com.kweaver.dip.domain.usecase.plan

import com.kweaver.dip.data.model.CronJob
import com.kweaver.dip.data.model.CronSchedule
import com.kweaver.dip.data.repository.PlanRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class ListPlansUseCaseTest {

    @Mock
    private lateinit var repository: PlanRepository

    private lateinit var useCase: ListPlansUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ListPlansUseCase(repository)
    }

    @Test
    fun `invoke returns plans on success`() = runTest {
        val expected = listOf(
            CronJob(
                id = "p1", agentId = "a1", sessionKey = "s1",
                name = "Daily", enabled = true,
                createdAtMs = 1000L, updatedAtMs = 2000L,
                schedule = CronSchedule(kind = "daily")
            )
        )
        whenever(repository.listPlans()).thenReturn(Result.success(expected))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke returns empty list`() = runTest {
        whenever(repository.listPlans()).thenReturn(Result.success(emptyList()))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke returns failure on error`() = runTest {
        whenever(repository.listPlans()).thenReturn(Result.failure(RuntimeException("Error")))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
