package com.kweaver.dip.ui.screens.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.Skill
import com.kweaver.dip.data.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillListUiState(
    val skills: List<Skill> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadStatus: String? = null
)

@HiltViewModel
class SkillListViewModel @Inject constructor(
    private val repository: SkillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillListUiState())
    val uiState: StateFlow<SkillListUiState> = _uiState

    init { loadSkills() }

    fun loadSkills() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.listSkills().fold(
                onSuccess = { _uiState.value = _uiState.value.copy(skills = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
        }
    }

    fun installSkill(fileName: String, bytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uploadStatus = "Uploading...")
            repository.installSkill(fileName, bytes).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(uploadStatus = "Installed: ${it.name}")
                    loadSkills()
                },
                onFailure = { _uiState.value = _uiState.value.copy(uploadStatus = "Failed: ${it.message}") }
            )
        }
    }
}
