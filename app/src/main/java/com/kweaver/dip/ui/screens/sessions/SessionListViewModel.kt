package com.kweaver.dip.ui.screens.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.Session
import com.kweaver.dip.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionListUiState(
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class SessionListViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionListUiState())
    val uiState: StateFlow<SessionListUiState> = _uiState

    init { loadSessions() }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.listSessions(search = _uiState.value.searchQuery.ifBlank { null }).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(sessions = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadSessions()
    }

    fun deleteSession(key: String) {
        viewModelScope.launch {
            repository.deleteSession(key)
            loadSessions()
        }
    }
}
