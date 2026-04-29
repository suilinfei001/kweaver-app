package com.kweaver.dip.ui.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.ApplicationInfo
import com.kweaver.dip.data.repository.AppRepository
import com.kweaver.dip.domain.usecase.store.ListAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppStoreUiState(
    val apps: List<ApplicationInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AppStoreViewModel @Inject constructor(
    private val listAppsUseCase: ListAppsUseCase,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppStoreUiState())
    val uiState: StateFlow<AppStoreUiState> = _uiState

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            listAppsUseCase().fold(
                onSuccess = { apps ->
                    _uiState.value = _uiState.value.copy(apps = apps, isLoading = false)
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
            listAppsUseCase().fold(
                onSuccess = { apps ->
                    _uiState.value = _uiState.value.copy(apps = apps, isRefreshing = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message, isRefreshing = false)
                }
            )
        }
    }

    fun togglePin(appKey: String, pinned: Boolean) {
        viewModelScope.launch {
            appRepository.pinApplication(appKey, pinned).fold(
                onSuccess = { updated ->
                    val current = _uiState.value.apps.toMutableList()
                    val index = current.indexOfFirst { it.key == appKey }
                    if (index >= 0) {
                        current[index] = updated
                        _uiState.value = _uiState.value.copy(apps = current)
                    }
                },
                onFailure = {}
            )
        }
    }

    fun uninstall(appKey: String) {
        viewModelScope.launch {
            appRepository.uninstallApplication(appKey).fold(
                onSuccess = {
                    val current = _uiState.value.apps.filter { it.key != appKey }
                    _uiState.value = _uiState.value.copy(apps = current)
                },
                onFailure = {}
            )
        }
    }
}
