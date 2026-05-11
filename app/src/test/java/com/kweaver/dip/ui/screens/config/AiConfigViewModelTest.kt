package com.kweaver.dip.ui.screens.config

import com.google.gson.Gson
import com.kweaver.dip.data.model.ModelsResponse
import com.kweaver.dip.data.repository.AiConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class AiConfigViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var repository: AiConfigRepository
    private lateinit var server: MockWebServer
    private lateinit var viewModel: AiConfigViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        server = MockWebServer()
        server.start()
        whenever(repository.config).thenReturn(MutableStateFlow(null))
        viewModel = AiConfigViewModel(repository, OkHttpClient(), Gson())
        viewModel.ioDispatcher = testDispatcher
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        server.shutdown()
    }

    @Test
    fun initialStateIsEmpty() {
        val state = viewModel.uiState.value
        assertEquals("", state.baseUrl)
        assertEquals("", state.modelId)
        assertEquals("", state.apiKey)
        assertEquals("4096", state.contextSize)
        assertNull(state.testSuccess)
        assertNull(state.testError)
    }

    @Test
    fun onBaseUrlChangeUpdatesState() {
        viewModel.onBaseUrlChange("http://localhost:8080")
        assertEquals("http://localhost:8080", viewModel.uiState.value.baseUrl)
    }

    @Test
    fun testConnectionSuccess() = runTest(testDispatcher) {
        server.enqueue(MockResponse().setBody(
            """{"data":[{"id":"gpt-test"}]}"""
        ))

        viewModel.onBaseUrlChange(server.url("").toString().trimEnd('/'))
        viewModel.onApiKeyChange("test-key")
        viewModel.testConnection()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.testSuccess!!)
        assertNull(viewModel.uiState.value.testError)
    }

    @Test
    fun testConnectionFailure() = runTest(testDispatcher) {
        server.enqueue(MockResponse().setResponseCode(500))

        viewModel.onBaseUrlChange(server.url("").toString().trimEnd('/'))
        viewModel.onApiKeyChange("test-key")
        viewModel.testConnection()

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.testSuccess ?: true)
        assertEquals("HTTP 500", viewModel.uiState.value.testError)
    }

    @Test
    fun testConnectionValidatesEmptyBaseUrl() = runTest(testDispatcher) {
        viewModel.testConnection()

        assertEquals("请填写 Base URL", viewModel.uiState.value.testError)
    }

    @Test
    fun testConnectionWorksWithEmptyApiKey() = runTest(testDispatcher) {
        server.enqueue(MockResponse().setBody(
            """{"data":[{"id":"local-model"}]}"""
        ))

        viewModel.onBaseUrlChange(server.url("").toString().trimEnd('/'))
        viewModel.onApiKeyChange("")
        viewModel.testConnection()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.testSuccess!!)
    }

    @Test
    fun saveConfigCallsRepository() = runTest(testDispatcher) {
        viewModel.onBaseUrlChange("http://localhost:8080")
        viewModel.onModelIdChange("gpt-test")
        viewModel.onApiKeyChange("test-key")
        viewModel.onContextSizeChange("8192")
        viewModel.saveConfig()

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saved)
        verify(repository).saveConfig(argThat {
            baseUrl == "http://localhost:8080" &&
                modelId == "gpt-test" &&
                apiKey == "test-key" &&
                contextSize == 8192
        })
    }
}
