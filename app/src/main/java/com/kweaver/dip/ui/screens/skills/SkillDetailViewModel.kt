package com.kweaver.dip.ui.screens.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.SkillTreeItem
import com.kweaver.dip.data.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillDetailUiState(
    val skillName: String = "",
    val content: String = "",
    val tree: List<SkillTreeItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SkillDetailViewModel @Inject constructor(
    private val repository: SkillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillDetailUiState())
    val uiState: StateFlow<SkillDetailUiState> = _uiState

    fun loadSkill(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, skillName = name)
            repository.getSkillContent(name).fold(
                onSuccess = { content ->
                    _uiState.value = _uiState.value.copy(content = content, isLoading = false)
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
            )
            repository.getSkillTree(name).fold(
                onSuccess = { tree -> _uiState.value = _uiState.value.copy(tree = tree) },
                onFailure = {}
            )
        }
    }
}
