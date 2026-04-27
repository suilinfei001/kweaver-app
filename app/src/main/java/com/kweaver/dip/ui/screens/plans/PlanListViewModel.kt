package com.kweaver.dip.ui.screens.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.CronJob
import com.kweaver.dip.data.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanListUiState(
    val plans: List<CronJob> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlanListViewModel @Inject constructor(
    private val repository: PlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanListUiState())
    val uiState: StateFlow<PlanListUiState> = _uiState

    init { loadPlans() }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.listPlans().fold(
                onSuccess = { _uiState.value = _uiState.value.copy(plans = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun toggleEnabled(plan: CronJob) {
        viewModelScope.launch {
            repository.updatePlan(plan.id, com.kweaver.dip.data.model.UpdatePlanRequest(enabled = !plan.enabled))
            loadPlans()
        }
    }
}
