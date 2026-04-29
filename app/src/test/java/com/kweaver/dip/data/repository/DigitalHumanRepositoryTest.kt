package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import retrofit2.Response

class DigitalHumanRepositoryTest {

    @Mock
    private lateinit var api: DipStudioApi

    @Mock
    private lateinit var tokenDataStore: com.kweaver.dip.data.local.datastore.TokenDataStore

    private lateinit var repository: DigitalHumanRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = DigitalHumanRepository(api, tokenDataStore)
    }

    @Test
    fun `listDigitalHumans returns list on success`() = runTest {
        val expected = listOf(
            DigitalHuman(id = "1", name = "Agent A"),
            DigitalHuman(id = "2", name = "Agent B")
        )
        whenever(api.listDigitalHumans()).thenReturn(expected)

        val result = repository.listDigitalHumans()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `listDigitalHumans returns empty list`() = runTest {
        whenever(api.listDigitalHumans()).thenReturn(emptyList())

        val result = repository.listDigitalHumans()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<DigitalHuman>(), result.getOrNull())
    }

    @Test
    fun `listDigitalHumans returns failure on exception`() = runTest {
        whenever(api.listDigitalHumans()).thenThrow(RuntimeException("Network error"))

        val result = repository.listDigitalHumans()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getDigitalHuman returns detail on success`() = runTest {
        val expected = DigitalHumanDetail(id = "1", name = "Agent A", soul = "prompt")
        whenever(api.getDigitalHuman("1")).thenReturn(expected)

        val result = repository.getDigitalHuman("1")

        assertTrue(result.isSuccess)
        assertEquals("Agent A", result.getOrNull()!!.name)
        assertEquals("prompt", result.getOrNull()!!.soul)
    }

    @Test
    fun `getDigitalHuman returns failure on exception`() = runTest {
        whenever(api.getDigitalHuman("999")).thenThrow(RuntimeException("Not found"))

        val result = repository.getDigitalHuman("999")

        assertTrue(result.isFailure)
    }

    @Test
    fun `createDigitalHuman returns result on success`() = runTest {
        val request = CreateDigitalHumanRequest(name = "New Agent")
        val expected = CreateDigitalHumanResult(id = "3", name = "New Agent")
        whenever(api.createDigitalHuman(request)).thenReturn(expected)

        val result = repository.createDigitalHuman(request)

        assertTrue(result.isSuccess)
        assertEquals("3", result.getOrNull()!!.id)
    }

    @Test
    fun `createDigitalHuman returns failure on exception`() = runTest {
        val request = CreateDigitalHumanRequest(name = "Bad Agent")
        whenever(api.createDigitalHuman(request)).thenThrow(RuntimeException("Server error"))

        val result = repository.createDigitalHuman(request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateDigitalHuman returns updated detail`() = runTest {
        val request = UpdateDigitalHumanRequest(name = "Updated Agent")
        val expected = DigitalHumanDetail(id = "1", name = "Updated Agent")
        whenever(api.updateDigitalHuman("1", request)).thenReturn(expected)

        val result = repository.updateDigitalHuman("1", request)

        assertTrue(result.isSuccess)
        assertEquals("Updated Agent", result.getOrNull()!!.name)
    }

    @Test
    fun `deleteDigitalHuman returns success on successful response`() = runTest {
        whenever(api.deleteDigitalHuman("1", false)).thenReturn(Response.success(Unit))

        val result = repository.deleteDigitalHuman("1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteDigitalHuman returns failure on unsuccessful response`() = runTest {
        whenever(api.deleteDigitalHuman("1", false)).thenReturn(Response.error(404, okhttp3.ResponseBody.create(null, "Not Found")))

        val result = repository.deleteDigitalHuman("1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteDigitalHuman with deleteFiles flag`() = runTest {
        whenever(api.deleteDigitalHuman("1", true)).thenReturn(Response.success(Unit))

        val result = repository.deleteDigitalHuman("1", deleteFiles = true)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `listBuiltIn returns list on success`() = runTest {
        val expected = listOf(
            BuiltInDigitalHuman(id = "b1", name = "Template A", isCreated = false)
        )
        whenever(api.listBuiltInDigitalHumans()).thenReturn(expected)

        val result = repository.listBuiltIn()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `createBuiltIn returns results on success`() = runTest {
        val expected = listOf(CreateDigitalHumanResult(id = "b1", name = "Template A"))
        whenever(api.createBuiltInDigitalHumans("b1")).thenReturn(expected)

        val result = repository.createBuiltIn("b1")

        assertTrue(result.isSuccess)
        assertEquals("b1", result.getOrNull()!!.first().id)
    }
}
