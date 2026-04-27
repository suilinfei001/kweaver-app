package com.kweaver.dip.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.DigitalHuman
import com.kweaver.dip.data.repository.DigitalHumanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val digitalHumans: List<DigitalHuman> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val username: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val digitalHumanRepository: DigitalHumanRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUsername()
        loadDigitalHumans()
    }

    private fun loadUsername() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(username = tokenDataStore.getUsername())
        }
    }

    fun loadDigitalHumans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            digitalHumanRepository.listDigitalHumans().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(digitalHumans = list, isLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                }
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            digitalHumanRepository.listDigitalHumans().fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(digitalHumans = list, isRefreshing = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message, isRefreshing = false)
                }
            )
        }
    }
}
