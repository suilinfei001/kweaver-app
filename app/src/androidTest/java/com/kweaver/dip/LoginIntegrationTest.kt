package com.kweaver.dip

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kweaver.dip.data.api.DipHubApi
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dipHubApi: DipHubApi

    @Test
    fun loginWithAdminCredentials_succeeds() = runBlocking {
        hiltRule.inject()

        val response = dipHubApi.login("admin", "eisoo.com")

        assertTrue(
            "Login should succeed, got ${response.code()}: ${response.errorBody()?.string()}",
            response.isSuccessful
        )
        assertEquals("Expected HTTP 200", 200, response.code())
    }
}
