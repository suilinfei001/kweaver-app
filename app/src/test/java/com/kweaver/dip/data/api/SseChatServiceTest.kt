package com.kweaver.dip.data.api

import com.google.gson.Gson
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SseChatServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: SseChatService

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        service = SseChatService(OkHttpClient(), Gson())
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun parsesSseDeltaContent() = runTest {
        server.enqueue(MockResponse().setBody(
            """
            data: {"choices":[{"delta":{"content":"Hello"}}]}

            data: {"choices":[{"delta":{"content":" world"}}]}

            data: [DONE]

            """.trimIndent()
        ).setHeader("Content-Type", "text/event-stream"))

        val chunks = service.streamChat(
            baseUrl = server.url("").toString(),
            apiKey = "test-key",
            model = "gpt-test",
            messages = listOf(com.kweaver.dip.data.model.ChatMessageDto("user", "hi")),
        ).toList()

        assertEquals(listOf("Hello", " world"), chunks)
    }

    @Test
    fun skipsChunksWithNoContent() = runTest {
        server.enqueue(MockResponse().setBody(
            """
            data: {"choices":[{"delta":{"role":"assistant"}}]}

            data: {"choices":[{"delta":{"content":"Hi"}}]}

            data: [DONE]

            """.trimIndent()
        ).setHeader("Content-Type", "text/event-stream"))

        val chunks = service.streamChat(
            baseUrl = server.url("").toString(),
            apiKey = "test-key",
            model = "gpt-test",
            messages = listOf(com.kweaver.dip.data.model.ChatMessageDto("user", "hi")),
        ).toList()

        assertEquals(listOf("Hi"), chunks)
    }

    @Test
    fun handlesMalformedJson() = runTest {
        server.enqueue(MockResponse().setBody(
            """
            data: {"choices":[{"delta":{"content":"ok"}}]}

            data: {broken json

            data: {"choices":[{"delta":{"content":"more"}}]}

            data: [DONE]

            """.trimIndent()
        ).setHeader("Content-Type", "text/event-stream"))

        val chunks = service.streamChat(
            baseUrl = server.url("").toString(),
            apiKey = "test-key",
            model = "gpt-test",
            messages = listOf(com.kweaver.dip.data.model.ChatMessageDto("user", "hi")),
        ).toList()

        assertEquals(listOf("ok", "more"), chunks)
    }

    @Test
    fun sendsAuthorizationHeader() = runTest {
        server.enqueue(MockResponse().setBody(
            "data: [DONE]\n\n"
        ).setHeader("Content-Type", "text/event-stream"))

        service.streamChat(
            baseUrl = server.url("").toString(),
            apiKey = "my-secret-key",
            model = "gpt-test",
            messages = listOf(com.kweaver.dip.data.model.ChatMessageDto("user", "hi")),
        ).toList()

        val request = server.takeRequest()
        assertEquals("Bearer my-secret-key", request.getHeader("Authorization"))
    }

    @Test
    fun emitsErrorOnConnectionFailure() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        var errorCaught = false
        try {
            service.streamChat(
                baseUrl = server.url("").toString(),
                apiKey = "test-key",
                model = "gpt-test",
                messages = listOf(com.kweaver.dip.data.model.ChatMessageDto("user", "hi")),
            ).toList()
        } catch (_: Exception) {
            errorCaught = true
        }

        assertTrue("Expected an error from 500 response", errorCaught)
    }
}
