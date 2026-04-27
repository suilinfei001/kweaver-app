package com.kweaver.dip.ui.screens.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.kweaver.dip.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
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

class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var authRepository: AuthRepository

    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        authRepository.stub {
            onBlocking { isLoggedIn() } doReturn false
        }
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateServerUrl updates state`() {
        viewModel.updateServerUrl("https://example.com")
        assertEquals("https://example.com", viewModel.uiState.value.serverUrl)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `updateUsername updates state`() {
        viewModel.updateUsername("admin")
        assertEquals("admin", viewModel.uiState.value.username)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `updatePassword updates state`() {
        viewModel.updatePassword("secret")
        assertEquals("secret", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login with blank username shows error`() = runTest {
        viewModel.updatePassword("pass")
        viewModel.login()
        assertEquals("Please enter username", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.loginSuccess)
    }

    @Test
    fun `login with blank password shows error`() = runTest {
        viewModel.updateUsername("admin")
        viewModel.login()
        assertEquals("Please enter password", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.loginSuccess)
    }

    @Test
    fun `login success updates state`() = runTest {
        whenever(authRepository.login("https://192.168.40.110", "admin", "eisoo.com"))
            .thenReturn(Result.success("token123"))

        viewModel.updateUsername("admin")
        viewModel.updatePassword("eisoo.com")
        viewModel.login()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loginSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.isLoggedIn.value)
    }

    @Test
    fun `login failure shows error`() = runTest {
        whenever(authRepository.login("https://192.168.40.110", "admin", "wrong"))
            .thenReturn(Result.failure(Exception("Invalid credentials")))

        viewModel.updateUsername("admin")
        viewModel.updatePassword("wrong")
        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loginSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Invalid credentials", viewModel.uiState.value.error)
        assertFalse(viewModel.isLoggedIn.value)
    }

    @Test
    fun `login sets loading then completes`() = runTest {
        whenever(authRepository.login("https://192.168.40.110", "admin", "pass"))
            .thenReturn(Result.success("token"))

        viewModel.updateUsername("admin")
        viewModel.updatePassword("pass")
        viewModel.login()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.loginSuccess)
        assertNull(viewModel.uiState.value.error)
    }
}
