package com.kweaver.dip.ui.screens.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kweaver.dip.data.local.datastore.TokenDataStore
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var digitalHumanRepository: DigitalHumanRepository

    @Mock
    private lateinit var tokenDataStore: TokenDataStore

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
    fun `init loads username and digital humans`() = runTest {
        whenever(tokenDataStore.getUsername()).thenReturn("admin")
        whenever(digitalHumanRepository.listDigitalHumans())
            .thenReturn(Result.success(listOf(DigitalHuman("1", "Agent1"))))

        val viewModel = HomeViewModel(digitalHumanRepository, tokenDataStore)
        advanceUntilIdle()

        assertEquals("admin", viewModel.uiState.value.username)
        assertEquals(1, viewModel.uiState.value.digitalHumans.size)
        assertEquals("Agent1", viewModel.uiState.value.digitalHumans[0].name)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadDigitalHumans handles error`() = runTest {
        whenever(tokenDataStore.getUsername()).thenReturn(null)
        whenever(digitalHumanRepository.listDigitalHumans())
            .thenReturn(Result.failure(Exception("Network error")))

        val viewModel = HomeViewModel(digitalHumanRepository, tokenDataStore)
        advanceUntilIdle()

        assertEquals("Network error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.digitalHumans.isEmpty())
    }

    @Test
    fun `refresh updates list`() = runTest {
        whenever(tokenDataStore.getUsername()).thenReturn(null)
        whenever(digitalHumanRepository.listDigitalHumans())
            .thenReturn(Result.success(emptyList()))

        val viewModel = HomeViewModel(digitalHumanRepository, tokenDataStore)
        advanceUntilIdle()

        val updatedList = listOf(DigitalHuman("2", "NewAgent"))
        whenever(digitalHumanRepository.listDigitalHumans())
            .thenReturn(Result.success(updatedList))

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(updatedList, viewModel.uiState.value.digitalHumans)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }
}
