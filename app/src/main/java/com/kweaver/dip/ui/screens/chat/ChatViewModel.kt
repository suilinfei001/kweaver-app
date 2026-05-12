package com.kweaver.dip.ui.screens.chat

import android.util.Log
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<MessageEntity> = emptyList(),
    val inputText: String = "",
    val streamingContent: String = "",
    val isStreaming: Boolean = false,
    val isRecording: Boolean = false,
    val error: String? = null,
    val conversationId: Long = 0,
    val conversations: List<ConversationEntity> = emptyList(),
    val config: AiConfig = AiConfig("", "", 4096, ""),
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val aiConfigRepository: AiConfigRepository,
    private val conversationDao: ConversationDao,
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConfig: AiConfig? = null

    init {
        viewModelScope.launch {
            aiConfigRepository.config.collect { config ->
                currentConfig = config
                _uiState.value = _uiState.value.copy(config = config ?: AiConfig("", "", 4096, ""))
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
                launch {
                    chatRepository.getMessages(id).collect { messages ->
                        _uiState.value = _uiState.value.copy(messages = messages)
                    }
                }
                doSendMessage(config, id, text)
            }
        } else {
            doSendMessage(config, conversationId, text)
        }
    }

    private fun doSendMessage(config: AiConfig, conversationId: Long, text: String) {
        viewModelScope.launch {
            Log.d(TAG, "=== doSendMessage Start ===")
            Log.d(TAG, "Config: baseUrl=${config.baseUrl}, model=${config.modelId}")
            Log.d(TAG, "ConversationId: $conversationId")
            Log.d(TAG, "User message: $text")

            _uiState.value = _uiState.value.copy(
                inputText = "",
                isStreaming = true,
                streamingContent = "",
                error = null,
            )

            val flow = chatRepository.sendMessage(config, conversationId, text)

            val sb = StringBuilder()
            var chunkCount = 0
            try {
                Log.d(TAG, "Starting to collect SSE flow...")
                flow.collect { chunk ->
                    sb.append(chunk)
                    chunkCount++
                    Log.v(TAG, "Chunk #$chunkCount received: '$chunk' (total length: ${sb.length})")
                    _uiState.value = _uiState.value.copy(streamingContent = sb.toString())
                }
                Log.d(TAG, "SSE flow completed. Total chunks: $chunkCount, total length: ${sb.length}")
            } catch (e: Exception) {
                Log.e(TAG, "SSE flow error: ${e.message}", e)
                Log.d(TAG, "Falling back to non-streaming request...")
                try {
                    val response = chatRepository.sendNonStreaming(config, conversationId, text)
                    Log.d(TAG, "Non-streaming response received, length: ${response.length}")
                    if (response.isNotEmpty()) {
                        chatRepository.saveAssistantMessage(conversationId, response)
                    }
                    val latestMessages = chatRepository.getMessages(conversationId).first()
                    _uiState.value = _uiState.value.copy(
                        isStreaming = false,
                        streamingContent = "",
                        messages = latestMessages,
                    )
                } catch (e2: Exception) {
                    Log.e(TAG, "Non-streaming also failed: ${e2.message}", e2)
                    _uiState.value = _uiState.value.copy(
                        isStreaming = false,
                        error = e2.message ?: "发送失败",
                    )
                }
                return@launch
            }

            val fullResponse = sb.toString()
            Log.d(TAG, "Saving assistant message, length: ${fullResponse.length}")
            if (fullResponse.isNotEmpty()) {
                chatRepository.saveAssistantMessage(conversationId, fullResponse)
            }

            val latestMessages = chatRepository.getMessages(conversationId).first()
            Log.d(TAG, "=== doSendMessage Complete ===")
            _uiState.value = _uiState.value.copy(
                isStreaming = false,
                streamingContent = "",
                messages = latestMessages,
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun startRecording() {
        _uiState.value = _uiState.value.copy(isRecording = true)
    }

    fun stopRecording() {
        _uiState.value = _uiState.value.copy(isRecording = false)
    }

    fun onSpeechResult(text: String) {
        if (text.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                inputText = _uiState.value.inputText + text,
                isRecording = false,
            )
            sendMessage()
        } else {
            _uiState.value = _uiState.value.copy(isRecording = false)
        }
    }

    fun onShortPress() {
        _uiState.value = _uiState.value.copy(error = "请按住按钮说话")
    }
}
