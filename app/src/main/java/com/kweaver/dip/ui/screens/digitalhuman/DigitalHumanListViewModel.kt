package com.kweaver.dip.ui.screens.digitalhuman

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.DigitalHuman
import com.kweaver.dip.data.repository.DigitalHumanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DigitalHumanListUiState(
    val digitalHumans: List<DigitalHuman> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DigitalHumanListViewModel @Inject constructor(
    private val repository: DigitalHumanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalHumanListUiState())
    val uiState: StateFlow<DigitalHumanListUiState> = _uiState.asStateFlow()

    init { loadDigitalHumans() }

    fun loadDigitalHumans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.listDigitalHumans().fold(
                onSuccess = { _uiState.value = _uiState.value.copy(digitalHumans = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun deleteDigitalHuman(id: String) {
        viewModelScope.launch {
            repository.deleteDigitalHuman(id)
            loadDigitalHumans()
        }
    }
}
