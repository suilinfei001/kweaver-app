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

class SkillRepositoryTest {

    @Mock
    private lateinit var api: DipStudioApi

    @Mock
    private lateinit var tokenDataStore: TokenDataStore

    private lateinit var repository: SkillRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = SkillRepository(api, tokenDataStore)
    }

    @Test
    fun `listSkills returns list on success`() = runTest {
        val expected = listOf(
            Skill(name = "search", description = "Web search", enabled = true),
            Skill(name = "code", description = "Code execution", enabled = false)
        )
        whenever(api.listSkills(null)).thenReturn(expected)

        val result = repository.listSkills()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertEquals("search", result.getOrNull()!![0].name)
    }

    @Test
    fun `listSkills with search filter`() = runTest {
        val expected = listOf(Skill(name = "search", description = "Web search"))
        whenever(api.listSkills("search")).thenReturn(expected)

        val result = repository.listSkills(search = "search")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `listSkills returns empty list`() = runTest {
        whenever(api.listSkills(null)).thenReturn(emptyList())

        val result = repository.listSkills()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Skill>(), result.getOrNull())
    }

    @Test
    fun `listSkills returns failure on exception`() = runTest {
        whenever(api.listSkills(null)).thenThrow(RuntimeException("Network error"))

        val result = repository.listSkills()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getSkillTree returns tree on success`() = runTest {
        val expected = listOf(
            SkillTreeItem(name = "root", path = "/", type = "dir", children = listOf(
                SkillTreeItem(name = "SKILL.md", path = "/SKILL.md", type = "file", children = null)
            ))
        )
        whenever(api.getSkillTree("search")).thenReturn(expected)

        val result = repository.getSkillTree("search")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals("root", result.getOrNull()!![0].name)
    }

    @Test
    fun `getSkillTree returns failure on exception`() = runTest {
        whenever(api.getSkillTree("unknown")).thenThrow(RuntimeException("Not found"))

        val result = repository.getSkillTree("unknown")

        assertTrue(result.isFailure)
    }

    @Test
    fun `getSkillContent returns content string on success`() = runTest {
        whenever(api.getSkillContent("search", "SKILL.md"))
            .thenReturn(mapOf("content" to "# Search Skill\nDoes web search"))

        val result = repository.getSkillContent("search")

        assertTrue(result.isSuccess)
        assertEquals("# Search Skill\nDoes web search", result.getOrNull())
    }

    @Test
    fun `getSkillContent returns empty string when content key missing`() = runTest {
        whenever(api.getSkillContent("search", "SKILL.md"))
            .thenReturn(mapOf("other" to "value"))

        val result = repository.getSkillContent("search")

        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull())
    }

    @Test
    fun `getSkillContent returns failure on exception`() = runTest {
        whenever(api.getSkillContent("search", "SKILL.md"))
            .thenThrow(RuntimeException("Error"))

        val result = repository.getSkillContent("search")

        assertTrue(result.isFailure)
    }

    @Test
    fun `installSkill returns failure when API fails`() = runTest {
        whenever(api.installSkill(org.mockito.kotlin.any()))
            .thenThrow(RuntimeException("Upload error"))

        val result = repository.installSkill("skill.zip", byteArrayOf(1, 2, 3))

        assertTrue(result.isFailure)
    }

    @Test
    fun `uninstallSkill returns success on success`() = runTest {
        whenever(api.uninstallSkill("search")).thenReturn(mapOf("status" to "ok"))

        val result = repository.uninstallSkill("search")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `uninstallSkill returns failure on exception`() = runTest {
        whenever(api.uninstallSkill("search")).thenThrow(RuntimeException("Error"))

        val result = repository.uninstallSkill("search")

        assertTrue(result.isFailure)
    }

    @Test
    fun `listDigitalHumanSkills returns list on success`() = runTest {
        val expected = listOf(
            DigitalHumanAgentSkill(name = "search", enabled = true),
            DigitalHumanAgentSkill(name = "code", enabled = false)
        )
        whenever(api.listDigitalHumanSkills("agent1")).thenReturn(expected)

        val result = repository.listDigitalHumanSkills("agent1")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun `listDigitalHumanSkills returns failure on exception`() = runTest {
        whenever(api.listDigitalHumanSkills("agent1")).thenThrow(RuntimeException("Error"))

        val result = repository.listDigitalHumanSkills("agent1")

        assertTrue(result.isFailure)
    }
}
