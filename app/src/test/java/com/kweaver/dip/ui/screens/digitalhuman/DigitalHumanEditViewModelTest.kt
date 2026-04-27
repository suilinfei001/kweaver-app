package com.kweaver.dip.ui.screens.digitalhuman

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kweaver.dip.data.model.DigitalHumanDetail
import com.kweaver.dip.data.model.Skill
import com.kweaver.dip.data.repository.DigitalHumanRepository
import com.kweaver.dip.data.repository.SkillRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class DigitalHumanEditViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var digitalHumanRepository: DigitalHumanRepository

    @Mock
    private lateinit var skillRepository: SkillRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init with null id sets create mode`() = runTest {
        whenever(skillRepository.listSkills()).thenReturn(Result.success(emptyList()))

        val viewModel = DigitalHumanEditViewModel(digitalHumanRepository, skillRepository)
        viewModel.init(null)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isEdit)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `init with id loads existing digital human`() = runTest {
        val detail = DigitalHumanDetail(
            id = "dh1", name = "TestAgent", creature = "Analyst",
            soul = "desc", bkn = emptyList(), skills = listOf("skill1"),
            channel = null
        )
        whenever(skillRepository.listSkills()).thenReturn(Result.success(listOf(Skill("skill1"))))
        whenever(digitalHumanRepository.getDigitalHuman("dh1")).thenReturn(Result.success(detail))

        val viewModel = DigitalHumanEditViewModel(digitalHumanRepository, skillRepository)
        viewModel.init("dh1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isEdit)
        assertEquals("TestAgent", viewModel.uiState.value.name)
        assertEquals("Analyst", viewModel.uiState.value.creature)
        assertEquals(listOf("skill1"), viewModel.uiState.value.selectedSkills)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `updateName updates state`() = runTest {
        whenever(skillRepository.listSkills()).thenReturn(Result.success(emptyList()))
        val viewModel = DigitalHumanEditViewModel(digitalHumanRepository, skillRepository)
        viewModel.updateName("NewName")
        assertEquals("NewName", viewModel.uiState.value.name)
    }

    @Test
    fun `addBknEntry adds empty entry`() = runTest {
        whenever(skillRepository.listSkills()).thenReturn(Result.success(emptyList()))
        val viewModel = DigitalHumanEditViewModel(digitalHumanRepository, skillRepository)
        viewModel.addBknEntry()
        assertEquals(1, viewModel.uiState.value.bknEntries.size)
    }

    @Test
    fun `removeBknEntry removes at index`() = runTest {
        whenever(skillRepository.listSkills()).thenReturn(Result.success(emptyList()))
        val viewModel = DigitalHumanEditViewModel(digitalHumanRepository, skillRepository)
        viewModel.addBknEntry()
        viewModel.updateBknEntry(0, "KB1", "http://kb1.com")
        viewModel.addBknEntry()
        viewModel.updateBknEntry(1, "KB2", "http://kb2.com")
        viewModel.removeBknEntry(0)
        assertEquals(1, viewModel.uiState.value.bknEntries.size)
        assertEquals("KB2", viewModel.uiState.value.bknEntries[0].name)
    }

    @Test
    fun `toggleSkill adds and removes skill`() = runTest {
        whenever(skillRepository.listSkills()).thenReturn(Result.success(emptyList()))
        val viewModel = DigitalHumanEditViewModel(digitalHumanRepository, skillRepository)
        viewModel.toggleSkill("skill1")
        assertTrue(viewModel.uiState.value.selectedSkills.contains("skill1"))
        viewModel.toggleSkill("skill1")
        assertFalse(viewModel.uiState.value.selectedSkills.contains("skill1"))
    }

    @Test
    fun `save with blank name shows error`() = runTest {
        whenever(skillRepository.listSkills()).thenReturn(Result.success(emptyList()))
        val viewModel = DigitalHumanEditViewModel(digitalHumanRepository, skillRepository)
        viewModel.save(null)
        assertEquals("Name is required", viewModel.uiState.value.error)
    }
}
