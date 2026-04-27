package com.kweaver.dip.data.model

data class Session(
    val key: String,
    val label: String? = null,
    val agentId: String? = null,
    val createdAtMs: Long? = null,
    val updatedAtMs: Long? = null,
    val messageCount: Int? = null,
    val preview: String? = null
)

data class SessionSummary(
    val key: String,
    val label: String? = null,
    val agentId: String? = null,
    val createdAtMs: Long? = null,
    val updatedAtMs: Long? = null,
    val messageCount: Int? = null
)

data class SessionMessage(
    val id: String,
    val role: String,
    val content: String,
    val timestamp: Long? = null,
    val toolCalls: List<ToolCallInfo>? = null
)

data class ToolCallInfo(
    val name: String,
    val arguments: String? = null,
    val result: String? = null,
    val status: String? = null
)
