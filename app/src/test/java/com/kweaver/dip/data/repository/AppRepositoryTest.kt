package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipHubApi
import com.kweaver.dip.data.model.ApplicationInfo
import com.kweaver.dip.data.model.PinAppRequest
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response

class AppRepositoryTest {

    @Mock
    private lateinit var api: DipHubApi

    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = AppRepository(api)
    }

    @Test
    fun `listApplications returns list on success`() = runTest {
        val expected = listOf(
            ApplicationInfo(id = 1, key = "app1", name = "My App", pinned = true),
            ApplicationInfo(id = 2, key = "app2", name = "Another App")
        )
        whenever(api.listApplications()).thenReturn(expected)

        val result = repository.listApplications()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertEquals("app1", result.getOrNull()!![0].key)
    }

    @Test
    fun `listApplications returns empty list`() = runTest {
        whenever(api.listApplications()).thenReturn(emptyList())

        val result = repository.listApplications()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<ApplicationInfo>(), result.getOrNull())
    }

    @Test
    fun `listApplications returns failure on exception`() = runTest {
        whenever(api.listApplications()).thenThrow(RuntimeException("Network error"))

        val result = repository.listApplications()

        assertTrue(result.isFailure)
    }

    @Test
    fun `uninstallApplication returns success on successful response`() = runTest {
        whenever(api.uninstallApplication("app1")).thenReturn(Response.success(Unit))

        val result = repository.uninstallApplication("app1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `uninstallApplication returns failure on unsuccessful response`() = runTest {
        whenever(api.uninstallApplication("app1"))
            .thenReturn(Response.error(404, ResponseBody.create(null, "Not Found")))

        val result = repository.uninstallApplication("app1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `uninstallApplication returns failure on exception`() = runTest {
        whenever(api.uninstallApplication("app1")).thenThrow(RuntimeException("Error"))

        val result = repository.uninstallApplication("app1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `pinApplication returns updated app on success`() = runTest {
        val expected = ApplicationInfo(id = 1, key = "app1", name = "My App", pinned = true)
        whenever(api.pinApplication("app1", PinAppRequest(pinned = true))).thenReturn(expected)

        val result = repository.pinApplication("app1", true)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.pinned)
    }

    @Test
    fun `pinApplication unpin returns updated app`() = runTest {
        val expected = ApplicationInfo(id = 1, key = "app1", name = "My App", pinned = false)
        whenever(api.pinApplication("app1", PinAppRequest(pinned = false))).thenReturn(expected)

        val result = repository.pinApplication("app1", false)

        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!.pinned)
    }

    @Test
    fun `pinApplication returns failure on exception`() = runTest {
        whenever(api.pinApplication("app1", PinAppRequest(pinned = true)))
            .thenThrow(RuntimeException("Error"))

        val result = repository.pinApplication("app1", true)

        assertTrue(result.isFailure)
    }
}
