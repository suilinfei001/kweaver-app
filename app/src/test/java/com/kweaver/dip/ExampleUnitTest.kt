package com.kweaver.dip

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Skeleton unit test demonstrating the test pattern.
 *
 * When adding business code (ViewModels, Repositories, UseCases),
 * follow these conventions:
 * - Use Mockito for mocking dependencies
 * - Use runTest from kotlinx-coroutines-test for suspend functions
 * - Use InstantTaskExecutorRule for LiveData/StateFlow tests
 * - Use StandardTestDispatcher and setMain/resetMain for coroutine testing
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
