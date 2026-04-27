package com.kweaver.dip.ui.screens.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.GuideStatus
import com.kweaver.dip.data.model.InitializeGuideRequest
import com.kweaver.dip.data.repository.GuideRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuideUiState(
    val status: GuideStatus? = null,
    val openclawAddress: String = "",
    val openclawToken: String = "",
    val kweaverBaseUrl: String = "",
    val kweaverToken: String = "",
    val step: Int = 0,
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GuideViewModel @Inject constructor(
    private val repository: GuideRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState: StateFlow<GuideUiState> = _uiState

    init {
        checkStatus()
    }

    private fun checkStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getStatus().fold(
                onSuccess = { status ->
                    _uiState.value = _uiState.value.copy(
                        status = status,
                        isLoading = false,
                        isInitialized = status.isInitialized,
                        step = if (status.isInitialized) 3 else 1
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun updateOpenclawAddress(addr: String) { _uiState.value = _uiState.value.copy(openclawAddress = addr) }
    fun updateOpenclawToken(token: String) { _uiState.value = _uiState.value.copy(openclawToken = token) }
    fun updateKweaverBaseUrl(url: String) { _uiState.value = _uiState.value.copy(kweaverBaseUrl = url) }
    fun updateKweaverToken(token: String) { _uiState.value = _uiState.value.copy(kweaverToken = token) }

    fun nextStep() {
        _uiState.value = _uiState.value.copy(step = minOf(_uiState.value.step + 1, 3))
    }

    fun prevStep() {
        _uiState.value = _uiState.value.copy(step = maxOf(_uiState.value.step - 1, 0))
    }

    fun initialize() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.initialize(
                InitializeGuideRequest(
                    openclawAddress = _uiState.value.openclawAddress,
                    openclawToken = _uiState.value.openclawToken,
                    kweaverBaseUrl = _uiState.value.kweaverBaseUrl.ifBlank { null },
                    kweaverToken = _uiState.value.kweaverToken.ifBlank { null }
                )
            ).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, isInitialized = true, step = 3)
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }
}
