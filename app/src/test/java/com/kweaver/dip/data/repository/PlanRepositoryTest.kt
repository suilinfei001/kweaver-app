package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response

class PlanRepositoryTest {

    @Mock
    private lateinit var api: DipStudioApi

    @Mock
    private lateinit var tokenDataStore: TokenDataStore

    private lateinit var repository: PlanRepository

    private val sampleSchedule = CronSchedule(kind = "daily", at = "09:00")

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = PlanRepository(api, tokenDataStore)
    }

    @Test
    fun `listPlans returns list on success`() = runTest {
        val expected = listOf(
            CronJob(
                id = "p1", agentId = "a1", sessionKey = "s1",
                name = "Daily Report", enabled = true,
                createdAtMs = 1000L, updatedAtMs = 2000L,
                schedule = sampleSchedule
            )
        )
        whenever(api.listPlans()).thenReturn(expected)

        val result = repository.listPlans()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals("Daily Report", result.getOrNull()!![0].name)
    }

    @Test
    fun `listPlans returns empty list`() = runTest {
        whenever(api.listPlans()).thenReturn(emptyList())

        val result = repository.listPlans()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<CronJob>(), result.getOrNull())
    }

    @Test
    fun `listPlans returns failure on exception`() = runTest {
        whenever(api.listPlans()).thenThrow(RuntimeException("Network error"))

        val result = repository.listPlans()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPlan returns plan on success`() = runTest {
        val expected = CronJob(
            id = "p1", agentId = "a1", sessionKey = "s1",
            name = "Daily Report", enabled = true,
            createdAtMs = 1000L, updatedAtMs = 2000L,
            schedule = sampleSchedule
        )
        whenever(api.getPlan("p1")).thenReturn(expected)

        val result = repository.getPlan("p1")

        assertTrue(result.isSuccess)
        assertEquals("p1", result.getOrNull()!!.id)
    }

    @Test
    fun `getPlan returns failure on exception`() = runTest {
        whenever(api.getPlan("invalid")).thenThrow(RuntimeException("Not found"))

        val result = repository.getPlan("invalid")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getPlanContent returns content string on success`() = runTest {
        whenever(api.getPlanContent("p1")).thenReturn(PlanContent(content = "plan content here"))

        val result = repository.getPlanContent("p1")

        assertTrue(result.isSuccess)
        assertEquals("plan content here", result.getOrNull())
    }

    @Test
    fun `getPlanContent returns failure on exception`() = runTest {
        whenever(api.getPlanContent("p1")).thenThrow(RuntimeException("Error"))

        val result = repository.getPlanContent("p1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `listPlanRuns returns runs on success`() = runTest {
        val expected = listOf(
            CronRunEntry(ts = 1000L, jobId = "p1", action = "execute", status = "success", summary = "Done")
        )
        whenever(api.listPlanRuns("p1", limit = 20)).thenReturn(expected)

        val result = repository.listPlanRuns("p1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals("success", result.getOrNull()!![0].status)
    }

    @Test
    fun `listPlanRuns returns failure on exception`() = runTest {
        whenever(api.listPlanRuns("p1", limit = 20)).thenThrow(RuntimeException("Error"))

        val result = repository.listPlanRuns("p1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `updatePlan returns updated plan`() = runTest {
        val request = UpdatePlanRequest(name = "Updated Plan", enabled = false)
        val expected = CronJob(
            id = "p1", agentId = "a1", sessionKey = "s1",
            name = "Updated Plan", enabled = false,
            createdAtMs = 1000L, updatedAtMs = 3000L,
            schedule = sampleSchedule
        )
        whenever(api.updatePlan("p1", request)).thenReturn(expected)

        val result = repository.updatePlan("p1", request)

        assertTrue(result.isSuccess)
        assertEquals("Updated Plan", result.getOrNull()!!.name)
        assertFalse(result.getOrNull()!!.enabled)
    }

    @Test
    fun `updatePlan returns failure on exception`() = runTest {
        val request = UpdatePlanRequest(name = "Updated")
        whenever(api.updatePlan("p1", request)).thenThrow(RuntimeException("Error"))

        val result = repository.updatePlan("p1", request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `deletePlan returns success on successful response`() = runTest {
        whenever(api.deletePlan("p1")).thenReturn(Response.success(Unit))

        val result = repository.deletePlan("p1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deletePlan returns failure on unsuccessful response`() = runTest {
        whenever(api.deletePlan("p1")).thenReturn(Response.error(404, okhttp3.ResponseBody.create(null, "Not Found")))

        val result = repository.deletePlan("p1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `listDigitalHumanPlans returns list on success`() = runTest {
        val expected = listOf(
            CronJob(
                id = "p1", agentId = "a1", sessionKey = "s1",
                name = "Plan 1", enabled = true,
                createdAtMs = 1000L, updatedAtMs = 2000L,
                schedule = sampleSchedule
            )
        )
        whenever(api.listDigitalHumanPlans("a1")).thenReturn(expected)

        val result = repository.listDigitalHumanPlans("a1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `listDigitalHumanPlans returns failure on exception`() = runTest {
        whenever(api.listDigitalHumanPlans("a1")).thenThrow(RuntimeException("Error"))

        val result = repository.listDigitalHumanPlans("a1")

        assertTrue(result.isFailure)
    }
}
