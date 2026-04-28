package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.OAuth2LoginHelper
import com.kweaver.dip.data.local.datastore.TokenDataStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AuthRepositoryTest {

    @Mock
    private lateinit var oAuth2LoginHelper: OAuth2LoginHelper

    @Mock
    private lateinit var tokenDataStore: TokenDataStore

    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepository(oAuth2LoginHelper, tokenDataStore)
    }

    @Test
    fun `login saves server url`() = runTest {
        whenever(oAuth2LoginHelper.login("https://example.com", "admin", "pass"))
            .thenReturn(OAuth2LoginHelper.LoginResult("token123", null))

        authRepository.login("https://example.com", "admin", "pass")

        verify(tokenDataStore).saveServerUrl("https://example.com")
    }

    @Test
    fun `login failure returns error`() = runTest {
        whenever(oAuth2LoginHelper.login("https://example.com", "admin", "wrong"))
            .thenThrow(RuntimeException("Signin POST failed with 401"))

        val result = authRepository.login("https://example.com", "admin", "wrong")

        assertTrue(result.isFailure)
    }

    @Test
    fun `login saves tokens and username on success`() = runTest {
        whenever(oAuth2LoginHelper.login("https://example.com", "admin", "pass"))
            .thenReturn(OAuth2LoginHelper.LoginResult("abc123", "refresh456"))

        val result = authRepository.login("https://example.com", "admin", "pass")

        assertTrue(result.isSuccess)
        assertEquals("abc123", result.getOrNull())
        verify(tokenDataStore).saveTokens("abc123", "refresh456")
        verify(tokenDataStore).saveUsername("admin")
    }

    @Test
    fun `login without refresh token`() = runTest {
        whenever(oAuth2LoginHelper.login("https://example.com", "admin", "pass"))
            .thenReturn(OAuth2LoginHelper.LoginResult("token_only", null))

        val result = authRepository.login("https://example.com", "admin", "pass")

        assertTrue(result.isSuccess)
        assertEquals("token_only", result.getOrNull())
        verify(tokenDataStore).saveTokens("token_only", null)
    }

    @Test
    fun `isLoggedIn returns true when token exists`() = runTest {
        whenever(tokenDataStore.getAccessToken()).thenReturn("token123")

        assertTrue(authRepository.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns false when token is null`() = runTest {
        whenever(tokenDataStore.getAccessToken()).thenReturn(null)

        assertFalse(authRepository.isLoggedIn())
    }

    @Test
    fun `logout clears all data`() = runTest {
        authRepository.logout()
        verify(tokenDataStore).clearAll()
    }
}
