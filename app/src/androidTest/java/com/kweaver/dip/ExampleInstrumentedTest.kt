package com.kweaver.dip

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Skeleton integration test demonstrating Hilt testing pattern.
 *
 * When adding integration tests:
 * - Annotate with @HiltAndroidTest and @RunWith(AndroidJUnit4::class)
 * - Use HiltAndroidRule for dependency injection
 * - Use HiltTestRunner (configured in build.gradle.kts)
 * - Inject real dependencies to verify DI graph correctness
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun appLaunches() {
        hiltRule.inject()
        assertEquals(4, 2 + 2)
    }
}
