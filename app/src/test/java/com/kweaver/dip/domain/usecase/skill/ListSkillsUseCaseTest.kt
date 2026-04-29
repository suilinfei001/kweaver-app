package com.kweaver.dip.domain.usecase.skill

import com.kweaver.dip.data.model.Skill
import com.kweaver.dip.data.repository.SkillRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class ListSkillsUseCaseTest {

    @Mock
    private lateinit var repository: SkillRepository

    private lateinit var useCase: ListSkillsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ListSkillsUseCase(repository)
    }

    @Test
    fun `invoke returns skills without filter`() = runTest {
        val expected = listOf(Skill(name = "search"), Skill(name = "code"))
        whenever(repository.listSkills(null)).thenReturn(Result.success(expected))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke filters by search`() = runTest {
        val expected = listOf(Skill(name = "search"))
        whenever(repository.listSkills("search")).thenReturn(Result.success(expected))

        val result = useCase(search = "search")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `invoke returns failure on error`() = runTest {
        whenever(repository.listSkills(null)).thenReturn(Result.failure(RuntimeException("Error")))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
