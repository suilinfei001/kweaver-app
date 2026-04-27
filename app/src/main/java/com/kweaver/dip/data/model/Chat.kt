package com.kweaver.dip.data.model

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long,
    val isStreaming: Boolean = false,
    val attachments: List<AttachmentInfo> = emptyList(),
    val toolCalls: List<ToolCallInfo> = emptyList()
)

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

data class AttachmentInfo(
    val name: String,
    val path: String
)

data class SseEvent(
    val event: String,
    val data: String
)

data class ChatUploadResult(
    val name: String,
    val path: String
)

data class CreateSessionKeyRequest(
    @SerializedName("agentId")
    val agentId: String
)

data class CreateSessionKeyResponse(
    @SerializedName("sessionKey")
    val sessionKey: String
)
