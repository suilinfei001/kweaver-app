package com.kweaver.dip.ui.screens.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kweaver.dip.data.model.AiConfig
import com.kweaver.dip.data.model.ModelsResponse
import com.kweaver.dip.data.repository.AiConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

data class AiConfigUiState(
    val baseUrl: String = "",
    val modelId: String = "",
    val apiKey: String = "",
    val contextSize: String = "4096",
    val isTesting: Boolean = false,
    val testSuccess: Boolean? = null,
    val testError: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

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
                    )
                }
            }
        }
    }

    fun onBaseUrlChange(value: String) {
        _uiState.value = _uiState.value.copy(baseUrl = value, testSuccess = null, testError = null)
    }

    fun onModelIdChange(value: String) {
        _uiState.value = _uiState.value.copy(modelId = value, testSuccess = null, testError = null)
    }

    fun onApiKeyChange(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value, testSuccess = null, testError = null)
    }

    fun onContextSizeChange(value: String) {
        _uiState.value = _uiState.value.copy(contextSize = value, testSuccess = null, testError = null)
    }

    fun testConnection() {
        val state = _uiState.value
        val baseUrl = state.baseUrl.trimEnd('/')
        val apiKey = state.apiKey.trim()
        if (baseUrl.isEmpty()) {
            _uiState.value = state.copy(testError = "请填写 Base URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTesting = true, testSuccess = null, testError = null)
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
                    _uiState.value = _uiState.value.copy(isTesting = false, testSuccess = false, testError = result)
                } else {
                    _uiState.value = _uiState.value.copy(isTesting = false, testSuccess = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isTesting = false, testSuccess = false, testError = e.message ?: "连接失败")
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
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            repository.saveConfig(config)
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }
}
