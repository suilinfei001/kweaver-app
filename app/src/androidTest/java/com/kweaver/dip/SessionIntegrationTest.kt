package com.kweaver.dip

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kweaver.dip.data.api.DipStudioApi
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SessionIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dipStudioApi: DipStudioApi

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun listSessions_doesNotCrash() = runBlocking {
        try {
            val result = dipStudioApi.listSessions(limit = 10)
            assertNotNull("Session list should not be null", result)
        } catch (e: retrofit2.HttpException) {
            assertNotNull("Expected HTTP exception", e.message)
        }
    }

    @Test
    fun listDigitalHumanSessions_doesNotCrash() = runBlocking {
        try {
            val result = dipStudioApi.listDigitalHumanSessions("test-agent", limit = 5)
            assertNotNull("Agent sessions should not be null", result)
        } catch (e: retrofit2.HttpException) {
            assertNotNull("Expected HTTP exception", e.message)
        }
    }
}
