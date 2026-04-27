package com.kweaver.dip.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String = "",
    val username: String? = null,
    val userId: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                serverUrl = tokenDataStore.getServerUrl(),
                username = tokenDataStore.getUsername(),
                userId = tokenDataStore.getUserId()
            )
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            tokenDataStore.saveServerUrl(url)
            _uiState.value = _uiState.value.copy(serverUrl = url)
        }
    }
}
