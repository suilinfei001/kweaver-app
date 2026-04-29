package com.kweaver.dip.ui.screens.digitalhuman

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.*
import com.kweaver.dip.data.repository.DigitalHumanRepository
import com.kweaver.dip.data.repository.SkillRepository
import com.kweaver.dip.domain.usecase.digitalhuman.SaveDigitalHumanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DigitalHumanEditUiState(
    val name: String = "",
    val creature: String = "",
    val soul: String = "",
    val bknEntries: List<BknEntry> = emptyList(),
    val selectedSkills: List<String> = emptyList(),
    val availableSkills: List<Skill> = emptyList(),
    val channelType: String = "",
    val channelAppId: String = "",
    val channelAppSecret: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val isEdit: Boolean = false
)

@HiltViewModel
class DigitalHumanEditViewModel @Inject constructor(
    private val digitalHumanRepository: DigitalHumanRepository,
    private val skillRepository: SkillRepository,
    private val saveDigitalHumanUseCase: SaveDigitalHumanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalHumanEditUiState())
    val uiState: StateFlow<DigitalHumanEditUiState> = _uiState

    fun init(digitalHumanId: String?) {
        viewModelScope.launch {
            skillRepository.listSkills().fold(
                onSuccess = { skills -> _uiState.value = _uiState.value.copy(availableSkills = skills) },
                onFailure = {}
            )

            if (digitalHumanId != null) {
                _uiState.value = _uiState.value.copy(isLoading = true, isEdit = true)
                digitalHumanRepository.getDigitalHuman(digitalHumanId).fold(
                    onSuccess = { detail ->
                        _uiState.value = _uiState.value.copy(
                            name = detail.name,
                            creature = detail.creature ?: "",
                            soul = detail.soul ?: "",
                            bknEntries = detail.bkn ?: emptyList(),
                            selectedSkills = detail.skills ?: emptyList(),
                            channelType = detail.channel?.type ?: "",
                            channelAppId = detail.channel?.appId ?: "",
                            channelAppSecret = detail.channel?.appSecret ?: "",
                            isLoading = false
                        )
                    },
                    onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
                )
            }
        }
    }

    fun updateName(name: String) { _uiState.value = _uiState.value.copy(name = name) }
    fun updateCreature(creature: String) { _uiState.value = _uiState.value.copy(creature = creature) }
    fun updateSoul(soul: String) { _uiState.value = _uiState.value.copy(soul = soul) }

    fun addBknEntry() {
        _uiState.value = _uiState.value.copy(
            bknEntries = _uiState.value.bknEntries + BknEntry("", "")
        )
    }

    fun updateBknEntry(index: Int, name: String, url: String) {
        val entries = _uiState.value.bknEntries.toMutableList()
        if (index in entries.indices) {
            entries[index] = BknEntry(name, url)
            _uiState.value = _uiState.value.copy(bknEntries = entries)
        }
    }

    fun removeBknEntry(index: Int) {
        val entries = _uiState.value.bknEntries.toMutableList()
        if (index in entries.indices) {
            entries.removeAt(index)
            _uiState.value = _uiState.value.copy(bknEntries = entries)
        }
    }

    fun toggleSkill(skillName: String) {
        val current = _uiState.value.selectedSkills.toMutableList()
        if (skillName in current) current.remove(skillName) else current.add(skillName)
        _uiState.value = _uiState.value.copy(selectedSkills = current)
    }

    fun updateChannelType(type: String) { _uiState.value = _uiState.value.copy(channelType = type) }
    fun updateChannelAppId(appId: String) { _uiState.value = _uiState.value.copy(channelAppId = appId) }
    fun updateChannelAppSecret(secret: String) { _uiState.value = _uiState.value.copy(channelAppSecret = secret) }

    fun save(digitalHumanId: String?) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val result = saveDigitalHumanUseCase(
                id = digitalHumanId,
                name = state.name,
                creature = state.creature,
                soul = state.soul,
                bknEntries = state.bknEntries,
                selectedSkills = state.selectedSkills,
                channelType = state.channelType,
                channelAppId = state.channelAppId,
                channelAppSecret = state.channelAppSecret
            )

            result.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, error = it.message) }
            )
        }
    }
}
