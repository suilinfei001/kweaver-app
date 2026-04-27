package com.kweaver.dip.ui.screens.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.CronJob
import com.kweaver.dip.data.model.CronRunEntry
import com.kweaver.dip.data.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanDetailUiState(
    val plan: CronJob? = null,
    val content: String = "",
    val runs: List<CronRunEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    private val repository: PlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanDetailUiState())
    val uiState: StateFlow<PlanDetailUiState> = _uiState

    fun loadPlan(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getPlan(id).fold(
                onSuccess = { plan ->
                    _uiState.value = _uiState.value.copy(plan = plan, isLoading = false)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
            repository.getPlanContent(id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(content = it) },
                onFailure = {}
            )
            repository.listPlanRuns(id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(runs = it) },
                onFailure = {}
            )
        }
    }
}
