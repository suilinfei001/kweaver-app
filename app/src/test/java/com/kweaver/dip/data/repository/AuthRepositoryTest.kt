package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipHubApi
import com.kweaver.dip.data.local.datastore.TokenDataStore
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class AuthRepositoryTest {

    @Mock
    private lateinit var dipHubApi: DipHubApi

    @Mock
    private lateinit var tokenDataStore: TokenDataStore

    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepository(dipHubApi, tokenDataStore)
    }

    @Test
    fun `login saves server url`() = runTest {
        whenever(tokenDataStore.getAccessToken()).thenReturn(null)
        val headers = Headers.headersOf()
        whenever(dipHubApi.login("admin", "pass"))
            .thenReturn(Response.success("ok".toResponseBody(), headers))

        authRepository.login("https://example.com", "admin", "pass")

        verify(tokenDataStore).saveServerUrl("https://example.com")
    }

    @Test
    fun `login failure returns error`() = runTest {
        whenever(tokenDataStore.getAccessToken()).thenReturn(null)
        whenever(dipHubApi.login("admin", "wrong"))
            .thenReturn(Response.error(401, "Unauthorized".toResponseBody()))

        val result = authRepository.login("https://example.com", "admin", "wrong")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Login failed") == true)
    }

    @Test
    fun `login with cookie token saves tokens`() = runTest {
        whenever(tokenDataStore.getAccessToken()).thenReturn(null)
        val headers = Headers.headersOf(
            "Set-Cookie", "dip.oauth2_token=abc123; Path=/",
            "Set-Cookie", "dip.refresh_token=refresh456; Path=/"
        )
        whenever(dipHubApi.login("admin", "pass"))
            .thenReturn(Response.success("".toResponseBody(), headers))

        val result = authRepository.login("https://example.com", "admin", "pass")

        assertTrue(result.isSuccess)
        assertEquals("abc123", result.getOrNull())
        verify(tokenDataStore).saveTokens("abc123", "refresh456")
        verify(tokenDataStore).saveUsername("admin")
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
