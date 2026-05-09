package com.kweaver.dip.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kweaver.dip.data.local.ConversationDao
import com.kweaver.dip.data.model.AiConfig
import com.kweaver.dip.data.model.ConversationEntity
import com.kweaver.dip.data.model.MessageEntity
import com.kweaver.dip.data.repository.AiConfigRepository
import com.kweaver.dip.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val inputText: String = "",
    val streamingContent: String = "",
    val isStreaming: Boolean = false,
    val error: String? = null,
    val conversationId: Long = 0,
    val conversations: List<ConversationEntity> = emptyList(),
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val aiConfigRepository: AiConfigRepository,
    private val conversationDao: ConversationDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConfig: AiConfig? = null

    init {
        viewModelScope.launch {
            aiConfigRepository.config.collect { config ->
                currentConfig = config
            }
        }
        viewModelScope.launch {
            conversationDao.getAll().collect { conversations ->
                _uiState.value = _uiState.value.copy(conversations = conversations)
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun loadConversation(conversationId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(conversationId = conversationId)
            chatRepository.getMessages(conversationId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun newConversation() {
        viewModelScope.launch {
            val id = conversationDao.insert(ConversationEntity(title = "新对话"))
            _uiState.value = _uiState.value.copy(
                conversationId = id,
                messages = emptyList(),
                streamingContent = "",
                error = null,
            )
            chatRepository.getMessages(id).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return
        if (_uiState.value.isStreaming) return

        val config = currentConfig ?: run {
            _uiState.value = _uiState.value.copy(error = "请先配置 AI 模型")
            return
        }

        val conversationId = _uiState.value.conversationId
        if (conversationId == 0L) {
            viewModelScope.launch {
                val id = conversationDao.insert(ConversationEntity(title = text.take(50)))
                _uiState.value = _uiState.value.copy(conversationId = id)
                doSendMessage(config, id, text)
            }
        } else {
            doSendMessage(config, conversationId, text)
        }
    }

    private fun doSendMessage(config: AiConfig, conversationId: Long, text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                inputText = "",
                isStreaming = true,
                streamingContent = "",
                error = null,
            )

            val flow = chatRepository.sendMessage(config, conversationId, text)

            // Collect streamed tokens
            val sb = StringBuilder()
            try {
                flow.collect { chunk ->
                    sb.append(chunk)
                    _uiState.value = _uiState.value.copy(streamingContent = sb.toString())
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    error = e.message ?: "发送失败",
                )
                return@launch
            }

            // Save full assistant response
            val fullResponse = sb.toString()
            if (fullResponse.isNotEmpty()) {
                chatRepository.saveAssistantMessage(conversationId, fullResponse)
            }

            _uiState.value = _uiState.value.copy(
                isStreaming = false,
                streamingContent = "",
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
