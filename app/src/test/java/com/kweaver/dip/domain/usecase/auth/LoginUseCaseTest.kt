package com.kweaver.dip.domain.usecase.auth

import com.kweaver.dip.data.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class LoginUseCaseTest {

    @Mock
    private lateinit var authRepository: AuthRepository

    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun `invoke returns token on success`() = runTest {
        whenever(authRepository.login("https://example.com", "admin", "pass"))
            .thenReturn(Result.success("token123"))

        val result = loginUseCase("https://example.com", "admin", "pass")

        assertTrue(result.isSuccess)
        assertEquals("token123", result.getOrNull())
    }

    @Test
    fun `invoke returns failure on wrong credentials`() = runTest {
        whenever(authRepository.login("https://example.com", "admin", "wrong"))
            .thenReturn(Result.failure(RuntimeException("401")))

        val result = loginUseCase("https://example.com", "admin", "wrong")

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns failure on network error`() = runTest {
        whenever(authRepository.login("https://example.com", "admin", "pass"))
            .thenReturn(Result.failure(RuntimeException("Network error")))

        val result = loginUseCase("https://example.com", "admin", "pass")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `isLoggedIn delegates to repository`() = runTest {
        whenever(authRepository.isLoggedIn()).thenReturn(true)

        assertTrue(loginUseCase.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns false when not logged in`() = runTest {
        whenever(authRepository.isLoggedIn()).thenReturn(false)

        assertFalse(loginUseCase.isLoggedIn())
    }

    @Test
    fun `logout delegates to repository`() = runTest {
        loginUseCase.logout()
        // No exception means success
    }
}
