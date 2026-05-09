package com.kweaver.dip.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kweaver.dip.data.model.AiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AiConfigDataStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dataStore: AiConfigDataStore

    @Before
    fun setup() {
        val prefs = PreferenceDataStoreFactory.createWithPath(
            produceFile = { tempFolder.root.absolutePath.toPath() / "test.preferences_pb" }
        )
        dataStore = AiConfigDataStore(prefs)
    }

    @Test
    fun `initial state has no config`() = runTest {
        val config = dataStore.config.first()
        assertNull(config)
        assertFalse(dataStore.hasConfig.first())
    }

    @Test
    fun `save and read config`() = runTest {
        val expected = AiConfig(
            baseUrl = "https://api.example.com",
            modelId = "gpt-4",
            contextSize = 8192,
            apiKey = "sk-test-key",
        )
        dataStore.saveConfig(expected)

        val actual = dataStore.config.first()
        assertEquals(expected, actual)
    }

    @Test
    fun `hasConfig returns true after save`() = runTest {
        dataStore.saveConfig(
            AiConfig(
                baseUrl = "https://api.example.com",
                modelId = "gpt-4",
                apiKey = "sk-test",
            )
        )
        assert(dataStore.hasConfig.first())
    }

    @Test
    fun `default contextSize is 4096`() = runTest {
        dataStore.saveConfig(
            AiConfig(
                baseUrl = "https://api.example.com",
                modelId = "gpt-4",
                contextSize = 4096,
                apiKey = "sk-test",
            )
        )
        val config = dataStore.config.first()
        assertEquals(4096, config?.contextSize)
    }

    @Test
    fun `overwrite config with new values`() = runTest {
        dataStore.saveConfig(
            AiConfig(
                baseUrl = "https://old.example.com",
                modelId = "gpt-3.5",
                contextSize = 2048,
                apiKey = "old-key",
            )
        )
        val newConfig = AiConfig(
            baseUrl = "https://new.example.com",
            modelId = "gpt-4",
            contextSize = 8192,
            apiKey = "new-key",
        )
        dataStore.saveConfig(newConfig)

        val actual = dataStore.config.first()
        assertEquals(newConfig, actual)
    }
}
