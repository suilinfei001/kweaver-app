package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.Session
import com.kweaver.dip.data.model.SessionMessage
import com.kweaver.dip.data.model.SessionSummary
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response

class SessionRepositoryTest {

    @Mock
    private lateinit var api: DipStudioApi

    @Mock
    private lateinit var tokenDataStore: TokenDataStore

    private lateinit var repository: SessionRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = SessionRepository(api, tokenDataStore)
    }

    @Test
    fun `listSessions returns list on success`() = runTest {
        val expected = listOf(
            Session(key = "s1", label = "Chat 1", agentId = "a1", messageCount = 5),
            Session(key = "s2", label = "Chat 2", agentId = "a2", messageCount = 3)
        )
        whenever(api.listSessions(limit = 50, search = null, agentId = null)).thenReturn(expected)

        val result = repository.listSessions()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertEquals("s1", result.getOrNull()!![0].key)
    }

    @Test
    fun `listSessions with search filter`() = runTest {
        val expected = listOf(Session(key = "s1", label = "Chat about AI"))
        whenever(api.listSessions(limit = 50, search = "AI", agentId = null)).thenReturn(expected)

        val result = repository.listSessions(search = "AI")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `listSessions with agentId filter`() = runTest {
        val expected = listOf(Session(key = "s1", agentId = "a1"))
        whenever(api.listSessions(limit = 50, search = null, agentId = "a1")).thenReturn(expected)

        val result = repository.listSessions(agentId = "a1")

        assertTrue(result.isSuccess)
        assertEquals("a1", result.getOrNull()!![0].agentId)
    }

    @Test
    fun `listSessions returns empty list`() = runTest {
        whenever(api.listSessions(limit = 50, search = null, agentId = null)).thenReturn(emptyList())

        val result = repository.listSessions()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Session>(), result.getOrNull())
    }

    @Test
    fun `listSessions returns failure on exception`() = runTest {
        whenever(api.listSessions(limit = 50, search = null, agentId = null))
            .thenThrow(RuntimeException("Network error"))

        val result = repository.listSessions()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getSessionSummary returns summary on success`() = runTest {
        val expected = SessionSummary(key = "s1", label = "Chat 1", messageCount = 10)
        whenever(api.getSessionSummary("s1")).thenReturn(expected)

        val result = repository.getSessionSummary("s1")

        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()!!.messageCount)
    }

    @Test
    fun `getSessionSummary returns failure on exception`() = runTest {
        whenever(api.getSessionSummary("invalid")).thenThrow(RuntimeException("Not found"))

        val result = repository.getSessionSummary("invalid")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getSessionMessages returns messages on success`() = runTest {
        val expected = listOf(
            SessionMessage(id = "m1", role = "user", content = "Hello"),
            SessionMessage(id = "m2", role = "assistant", content = "Hi there")
        )
        whenever(api.getSessionMessages("s1", limit = null)).thenReturn(expected)

        val result = repository.getSessionMessages("s1")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertEquals("Hello", result.getOrNull()!![0].content)
    }

    @Test
    fun `deleteSession returns success on successful response`() = runTest {
        whenever(api.deleteSession("s1")).thenReturn(Response.success(Unit))

        val result = repository.deleteSession("s1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteSession returns failure on unsuccessful response`() = runTest {
        whenever(api.deleteSession("s1")).thenReturn(Response.error(500, okhttp3.ResponseBody.create(null, "Error")))

        val result = repository.deleteSession("s1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `listDigitalHumanSessions returns list on success`() = runTest {
        val expected = listOf(Session(key = "s1", agentId = "a1"))
        whenever(api.listDigitalHumanSessions("a1", limit = 20)).thenReturn(expected)

        val result = repository.listDigitalHumanSessions("a1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `listDigitalHumanSessions returns failure on exception`() = runTest {
        whenever(api.listDigitalHumanSessions("a1", limit = 20))
            .thenThrow(RuntimeException("Error"))

        val result = repository.listDigitalHumanSessions("a1")

        assertTrue(result.isFailure)
    }
}
