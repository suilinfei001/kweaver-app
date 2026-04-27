package com.kweaver.dip.ui.screens.digitalhuman

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kweaver.dip.data.model.DigitalHuman
import com.kweaver.dip.data.repository.DigitalHumanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DigitalHumanListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: DigitalHumanRepository

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
    fun `init loads digital humans`() = runTest {
        val humans = listOf(DigitalHuman("1", "Agent1"), DigitalHuman("2", "Agent2"))
        whenever(repository.listDigitalHumans()).thenReturn(Result.success(humans))

        val viewModel = DigitalHumanListViewModel(repository)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.digitalHumans.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadDigitalHumans handles error`() = runTest {
        whenever(repository.listDigitalHumans())
            .thenReturn(Result.failure(Exception("Server error")))

        val viewModel = DigitalHumanListViewModel(repository)
        advanceUntilIdle()

        assertEquals("Server error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `deleteDigitalHuman calls repository`() = runTest {
        whenever(repository.listDigitalHumans()).thenReturn(Result.success(emptyList()))
        whenever(repository.deleteDigitalHuman("1")).thenReturn(Result.success(Unit))

        val viewModel = DigitalHumanListViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteDigitalHuman("1")
        advanceUntilIdle()

        verify(repository).deleteDigitalHuman("1")
    }
}
