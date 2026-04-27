package com.kweaver.dip.ui.screens.digitalhuman

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.DigitalHumanDetail
import com.kweaver.dip.data.repository.DigitalHumanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DigitalHumanDetailUiState(
    val detail: DigitalHumanDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DigitalHumanDetailViewModel @Inject constructor(
    private val repository: DigitalHumanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalHumanDetailUiState())
    val uiState: StateFlow<DigitalHumanDetailUiState> = _uiState

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getDigitalHuman(id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(detail = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }
}
