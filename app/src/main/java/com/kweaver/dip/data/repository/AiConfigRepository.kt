package com.kweaver.dip.data.repository

import com.kweaver.dip.data.local.AiConfigDataStore
import com.kweaver.dip.data.model.AiConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiConfigRepository @Inject constructor(
    private val dataStore: AiConfigDataStore,
) {
    val config: Flow<AiConfig?> = dataStore.config
    val hasConfig: Flow<Boolean> = dataStore.hasConfig

    suspend fun saveConfig(config: AiConfig) = dataStore.saveConfig(config)
}
