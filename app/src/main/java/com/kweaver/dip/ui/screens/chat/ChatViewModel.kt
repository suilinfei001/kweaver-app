package com.kweaver.dip.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.model.*
import com.kweaver.dip.domain.usecase.chat.CreateSessionUseCase
import com.kweaver.dip.domain.usecase.chat.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val streamingContent: String = "",
    val isStreaming: Boolean = false,
    val isSending: Boolean = false,
    val isCreatingSession: Boolean = false,
    val sessionKey: String? = null,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val createSessionUseCase: CreateSessionUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun initSession(agentId: String, existingSessionKey: String?) {
        if (existingSessionKey != null) {
            _uiState.value = _uiState.value.copy(sessionKey = existingSessionKey)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingSession = true)
            createSessionUseCase(agentId).fold(
                onSuccess = { key ->
                    _uiState.value = _uiState.value.copy(sessionKey = key, isCreatingSession = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message, isCreatingSession = false)
                }
            )
        }
    }

    fun sendMessage(text: String) {
        val sessionKey = _uiState.value.sessionKey ?: return
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            role = MessageRole.USER,
            content = text,
            timestamp = System.currentTimeMillis()
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isSending = true,
            isStreaming = true,
            streamingContent = "",
            error = null
        )

        viewModelScope.launch {
            try {
                sendMessageUseCase(sessionKey, text).collect { event ->
                    when (event.event) {
                        "error" -> {
                            _uiState.value = _uiState.value.copy(
                                isStreaming = false,
                                isSending = false,
                                error = event.data
                            )
                        }
                        "done" -> {
                            val content = _uiState.value.streamingContent
                            if (content.isNotBlank()) {
                                val assistantMessage = ChatMessage(
                                    id = "resp-${System.currentTimeMillis()}",
                                    role = MessageRole.ASSISTANT,
                                    content = content,
                                    timestamp = System.currentTimeMillis()
                                )
                                _uiState.value = _uiState.value.copy(
                                    messages = _uiState.value.messages + assistantMessage,
                                    streamingContent = "",
                                    isStreaming = false,
                                    isSending = false
                                )
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    streamingContent = "",
                                    isStreaming = false,
                                    isSending = false
                                )
                            }
                        }
                        else -> {
                            _uiState.value = _uiState.value.copy(
                                streamingContent = _uiState.value.streamingContent + event.data
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    isSending = false,
                    error = e.message
                )
            }
        }
    }
}
