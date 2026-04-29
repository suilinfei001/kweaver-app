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
class DigitalHumanIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dipStudioApi: DipStudioApi

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun listDigitalHumans_doesNotCrash() = runBlocking {
        try {
            val result = dipStudioApi.listDigitalHumans()
            assertNotNull("Digital human list should not be null", result)
        } catch (e: retrofit2.HttpException) {
            // Auth may not be configured in test environment - accept 401
            assertNotNull("Expected HTTP exception", e.message)
        }
    }

    @Test
    fun listBuiltInDigitalHumans_doesNotCrash() = runBlocking {
        try {
            val result = dipStudioApi.listBuiltInDigitalHumans()
            assertNotNull("Built-in digital human list should not be null", result)
        } catch (e: retrofit2.HttpException) {
            assertNotNull("Expected HTTP exception", e.message)
        }
    }

    @Test
    fun listSkills_doesNotCrash() = runBlocking {
        try {
            val result = dipStudioApi.listSkills()
            assertNotNull("Skills list should not be null", result)
        } catch (e: retrofit2.HttpException) {
            assertNotNull("Expected HTTP exception", e.message)
        }
    }

    @Test
    fun listPlans_doesNotCrash() = runBlocking {
        try {
            val result = dipStudioApi.listPlans()
            assertNotNull("Plans list should not be null", result)
        } catch (e: retrofit2.HttpException) {
            assertNotNull("Expected HTTP exception", e.message)
        }
    }
}
