package com.kweaver.dip.ui.screens.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kweaver.dip.data.model.AiConfig
import com.kweaver.dip.data.model.ModelsResponse
import com.kweaver.dip.data.repository.AiConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

enum class TestStatus {
    NONE,
    TESTING,
    SUCCESS,
    FAILED,
}

data class AiConfigUiState(
    val baseUrl: String = "",
    val modelId: String = "",
    val apiKey: String = "",
    val contextSize: String = "4096",
    val asrUrl: String = "",
    val asrEnabled: Boolean = false,
    val ttsUrl: String = "",
    val ttsEnabled: Boolean = false,
    val aiTestStatus: TestStatus = TestStatus.NONE,
    val aiTestError: String? = null,
    val asrTestStatus: TestStatus = TestStatus.NONE,
    val asrTestError: String? = null,
    val ttsTestStatus: TestStatus = TestStatus.NONE,
    val ttsTestError: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
) {
    val canSave: Boolean
        get() {
            if (baseUrl.isBlank()) return false
            if (asrEnabled && asrTestStatus != TestStatus.SUCCESS) return false
            if (ttsEnabled && ttsTestStatus != TestStatus.SUCCESS) return false
            if (aiTestStatus != TestStatus.SUCCESS) return false
            return true
        }
}

@HiltViewModel
class AiConfigViewModel @Inject constructor(
    private val repository: AiConfigRepository,
    private val client: OkHttpClient,
    private val gson: Gson,
) : ViewModel() {

    var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _uiState = MutableStateFlow(AiConfigUiState())
    val uiState: StateFlow<AiConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.config.collect { config ->
                if (config != null) {
                    _uiState.value = _uiState.value.copy(
                        baseUrl = config.baseUrl,
                        modelId = config.modelId,
                        apiKey = config.apiKey,
                        contextSize = config.contextSize.toString(),
                        asrUrl = config.asrUrl,
                        asrEnabled = config.asrEnabled,
                        ttsUrl = config.ttsUrl,
                        ttsEnabled = config.ttsEnabled,
                    )
                }
            }
        }
    }

    fun onBaseUrlChange(value: String) {
        _uiState.value = _uiState.value.copy(
            baseUrl = value,
            aiTestStatus = TestStatus.NONE,
            aiTestError = null,
        )
    }

    fun onModelIdChange(value: String) {
        _uiState.value = _uiState.value.copy(
            modelId = value,
            aiTestStatus = TestStatus.NONE,
            aiTestError = null,
        )
    }

    fun onApiKeyChange(value: String) {
        _uiState.value = _uiState.value.copy(
            apiKey = value,
            aiTestStatus = TestStatus.NONE,
            aiTestError = null,
        )
    }

    fun onContextSizeChange(value: String) {
        _uiState.value = _uiState.value.copy(
            contextSize = value,
            aiTestStatus = TestStatus.NONE,
            aiTestError = null,
        )
    }

    fun onAsrUrlChange(value: String) {
        _uiState.value = _uiState.value.copy(
            asrUrl = value,
            asrTestStatus = TestStatus.NONE,
            asrTestError = null,
        )
    }

    fun onAsrEnabledChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(asrEnabled = enabled)
    }

    fun onTtsUrlChange(value: String) {
        _uiState.value = _uiState.value.copy(
            ttsUrl = value,
            ttsTestStatus = TestStatus.NONE,
            ttsTestError = null,
        )
    }

    fun onTtsEnabledChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(ttsEnabled = enabled)
    }

    fun testAiConnection() {
        val state = _uiState.value
        val baseUrl = state.baseUrl.trimEnd('/')
        val apiKey = state.apiKey.trim()

        if (baseUrl.isEmpty()) {
            _uiState.value = state.copy(aiTestStatus = TestStatus.FAILED, aiTestError = "请填写 Base URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(aiTestStatus = TestStatus.TESTING, aiTestError = null)
            try {
                val result = withContext(ioDispatcher) {
                    val requestBuilder = Request.Builder()
                        .url("$baseUrl/models")
                        .get()
                    if (apiKey.isNotEmpty()) {
                        requestBuilder.header("Authorization", "Bearer $apiKey")
                    }
                    val request = requestBuilder.build()
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        return@withContext "HTTP ${response.code}"
                    }
                    val body = response.body?.string() ?: ""
                    val models = gson.fromJson(body, ModelsResponse::class.java)
                    if (models.data.isNotEmpty()) null else "没有可用模型"
                }
                if (result != null) {
                    _uiState.value = _uiState.value.copy(aiTestStatus = TestStatus.FAILED, aiTestError = result)
                    repository.setAiValidated(false)
                } else {
                    _uiState.value = _uiState.value.copy(aiTestStatus = TestStatus.SUCCESS)
                    repository.setAiValidated(true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    aiTestStatus = TestStatus.FAILED,
                    aiTestError = e.message ?: "连接失败",
                )
                repository.setAiValidated(false)
            }
        }
    }

    fun testAsrConnection() {
        val state = _uiState.value
        val asrUrl = state.asrUrl.trimEnd('/')

        if (asrUrl.isEmpty()) {
            _uiState.value = state.copy(asrTestStatus = TestStatus.FAILED, asrTestError = "请填写 ASR 服务地址")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(asrTestStatus = TestStatus.TESTING, asrTestError = null)
            try {
                val result = withContext(ioDispatcher) {
                    val request = Request.Builder()
                        .url("$asrUrl/health")
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) null else "HTTP ${response.code}"
                }
                if (result != null) {
                    _uiState.value = _uiState.value.copy(asrTestStatus = TestStatus.FAILED, asrTestError = result)
                    repository.setAsrValidated(false)
                } else {
                    _uiState.value = _uiState.value.copy(asrTestStatus = TestStatus.SUCCESS)
                    repository.setAsrValidated(true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    asrTestStatus = TestStatus.FAILED,
                    asrTestError = e.message ?: "连接失败",
                )
                repository.setAsrValidated(false)
            }
        }
    }

    fun testTtsConnection() {
        val state = _uiState.value
        val ttsUrl = state.ttsUrl.trimEnd('/')

        if (ttsUrl.isEmpty()) {
            _uiState.value = state.copy(ttsTestStatus = TestStatus.FAILED, ttsTestError = "请填写 TTS 服务地址")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(ttsTestStatus = TestStatus.TESTING, ttsTestError = null)
            try {
                val result = withContext(ioDispatcher) {
                    val request = Request.Builder()
                        .url("$ttsUrl/health")
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) null else "HTTP ${response.code}"
                }
                if (result != null) {
                    _uiState.value = _uiState.value.copy(ttsTestStatus = TestStatus.FAILED, ttsTestError = result)
                    repository.setTtsValidated(false)
                } else {
                    _uiState.value = _uiState.value.copy(ttsTestStatus = TestStatus.SUCCESS)
                    repository.setTtsValidated(true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    ttsTestStatus = TestStatus.FAILED,
                    ttsTestError = e.message ?: "连接失败",
                )
                repository.setTtsValidated(false)
            }
        }
    }

    fun saveConfig() {
        val state = _uiState.value
        val config = AiConfig(
            baseUrl = state.baseUrl.trimEnd('/'),
            modelId = state.modelId.trim(),
            apiKey = state.apiKey.trim(),
            contextSize = state.contextSize.toIntOrNull() ?: 4096,
            asrUrl = state.asrUrl.trim(),
            asrEnabled = state.asrEnabled,
            ttsUrl = state.ttsUrl.trim(),
            ttsEnabled = state.ttsEnabled,
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            repository.saveConfig(config)
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }
}