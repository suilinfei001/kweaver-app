package com.kweaver.dip.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kweaver.dip.data.model.AiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiConfigDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private companion object {
        val BASE_URL = stringPreferencesKey("base_url")
        val MODEL_ID = stringPreferencesKey("model_id")
        val CONTEXT_SIZE = intPreferencesKey("context_size")
        val API_KEY = stringPreferencesKey("api_key")
        val ASR_URL = stringPreferencesKey("asr_url")
        val ASR_ENABLED = booleanPreferencesKey("asr_enabled")
        val TTS_URL = stringPreferencesKey("tts_url")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val AI_VALIDATED = booleanPreferencesKey("ai_validated")
        val ASR_VALIDATED = booleanPreferencesKey("asr_validated")
        val TTS_VALIDATED = booleanPreferencesKey("tts_validated")
    }

    val config: Flow<AiConfig?> = dataStore.data.map { prefs ->
        val baseUrl = prefs[BASE_URL]
        val modelId = prefs[MODEL_ID]
        val apiKey = prefs[API_KEY]
        if (baseUrl != null && modelId != null && apiKey != null) {
            AiConfig(
                baseUrl = baseUrl,
                modelId = modelId,
                contextSize = prefs[CONTEXT_SIZE] ?: 4096,
                apiKey = apiKey,
                asrUrl = prefs[ASR_URL] ?: "",
                asrEnabled = prefs[ASR_ENABLED] ?: false,
                ttsUrl = prefs[TTS_URL] ?: "",
                ttsEnabled = prefs[TTS_ENABLED] ?: false,
            )
        } else {
            null
        }
    }

    val hasConfig: Flow<Boolean> = config.map { it != null }

    val isAiValidated: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[AI_VALIDATED] ?: false
    }

    val isAsrValidated: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ASR_VALIDATED] ?: false
    }

    val isTtsValidated: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[TTS_VALIDATED] ?: false
    }

    val isConfigFullyValidated: Flow<Boolean> = dataStore.data.map { prefs ->
        val aiValidated = prefs[AI_VALIDATED] ?: false
        val asrEnabled = prefs[ASR_ENABLED] ?: false
        val asrValidated = prefs[ASR_VALIDATED] ?: false
        val ttsEnabled = prefs[TTS_ENABLED] ?: false
        val ttsValidated = prefs[TTS_VALIDATED] ?: false

        if (!aiValidated) return@map false
        if (asrEnabled && !asrValidated) return@map false
        if (ttsEnabled && !ttsValidated) return@map false
        true
    }

    suspend fun saveConfig(config: AiConfig) {
        dataStore.edit { prefs ->
            prefs[BASE_URL] = config.baseUrl
            prefs[MODEL_ID] = config.modelId
            prefs[CONTEXT_SIZE] = config.contextSize
            prefs[API_KEY] = config.apiKey
            prefs[ASR_URL] = config.asrUrl
            prefs[ASR_ENABLED] = config.asrEnabled
            prefs[TTS_URL] = config.ttsUrl
            prefs[TTS_ENABLED] = config.ttsEnabled
        }
    }

    suspend fun setAiValidated(validated: Boolean) {
        dataStore.edit { prefs ->
            prefs[AI_VALIDATED] = validated
        }
    }

    suspend fun setAsrValidated(validated: Boolean) {
        dataStore.edit { prefs ->
            prefs[ASR_VALIDATED] = validated
        }
    }

    suspend fun setTtsValidated(validated: Boolean) {
        dataStore.edit { prefs ->
            prefs[TTS_VALIDATED] = validated
        }
    }
}