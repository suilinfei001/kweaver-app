package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.model.GuideStatus
import com.kweaver.dip.data.model.InitializeGuideRequest
import com.kweaver.dip.data.model.OpenClawConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response

class GuideRepositoryTest {

    @Mock
    private lateinit var api: DipStudioApi

    private lateinit var repository: GuideRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = GuideRepository(api)
    }

    @Test
    fun `getStatus returns status on success`() = runTest {
        val expected = GuideStatus(state = "initialized", isInitialized = true)
        whenever(api.getGuideStatus()).thenReturn(expected)

        val result = repository.getStatus()

        assertTrue(result.isSuccess)
        assertEquals("initialized", result.getOrNull()!!.state)
        assertTrue(result.getOrNull()!!.isInitialized)
    }

    @Test
    fun `getStatus returns uninitialized state`() = runTest {
        val expected = GuideStatus(state = "uninitialized", isInitialized = false)
        whenever(api.getGuideStatus()).thenReturn(expected)

        val result = repository.getStatus()

        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!.isInitialized)
    }

    @Test
    fun `getStatus returns failure on exception`() = runTest {
        whenever(api.getGuideStatus()).thenThrow(RuntimeException("Network error"))

        val result = repository.getStatus()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getOpenClawConfig returns config on success`() = runTest {
        val expected = OpenClawConfig(address = "http://localhost:8080", hasToken = true)
        whenever(api.getOpenClawConfig()).thenReturn(expected)

        val result = repository.getOpenClawConfig()

        assertTrue(result.isSuccess)
        assertEquals("http://localhost:8080", result.getOrNull()!!.address)
        assertTrue(result.getOrNull()!!.hasToken)
    }

    @Test
    fun `getOpenClawConfig returns config without token`() = runTest {
        val expected = OpenClawConfig(address = null, hasToken = false)
        whenever(api.getOpenClawConfig()).thenReturn(expected)

        val result = repository.getOpenClawConfig()

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull()!!.address)
        assertFalse(result.getOrNull()!!.hasToken)
    }

    @Test
    fun `getOpenClawConfig returns failure on exception`() = runTest {
        whenever(api.getOpenClawConfig()).thenThrow(RuntimeException("Error"))

        val result = repository.getOpenClawConfig()

        assertTrue(result.isFailure)
    }

    @Test
    fun `initialize returns success on successful response`() = runTest {
        val request = InitializeGuideRequest(
            openclawAddress = "http://localhost:8080",
            openclawToken = "token123"
        )
        whenever(api.initializeGuide(request)).thenReturn(Response.success(Unit))

        val result = repository.initialize(request)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `initialize returns failure on unsuccessful response`() = runTest {
        val request = InitializeGuideRequest(
            openclawAddress = "http://localhost:8080",
            openclawToken = "token123"
        )
        whenever(api.initializeGuide(request)).thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "Error")))

        val result = repository.initialize(request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `initialize returns failure on exception`() = runTest {
        val request = InitializeGuideRequest(
            openclawAddress = "http://localhost:8080",
            openclawToken = "token123"
        )
        whenever(api.initializeGuide(request)).thenThrow(RuntimeException("Connection refused"))

        val result = repository.initialize(request)

        assertTrue(result.isFailure)
        assertEquals("Connection refused", result.exceptionOrNull()?.message)
    }
}
