package com.kweaver.dip.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
            )
        } else {
            null
        }
    }

    suspend fun saveConfig(config: AiConfig) {
        dataStore.edit { prefs ->
            prefs[BASE_URL] = config.baseUrl
            prefs[MODEL_ID] = config.modelId
            prefs[CONTEXT_SIZE] = config.contextSize
            prefs[API_KEY] = config.apiKey
        }
    }

    val hasConfig: Flow<Boolean> = config.map { it != null }
}
