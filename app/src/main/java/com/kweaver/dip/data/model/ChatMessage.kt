package com.kweaver.dip.data.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val stream: Boolean = true,
)

data class ChatMessageDto(
    val role: String,
    val content: String,
)

data class ChatResponse(
    val choices: List<Choice>,
) {
    data class Choice(
        val message: Message? = null,
        val delta: Delta? = null,
    )

    data class Message(
        val role: String? = null,
        val content: String? = null,
    )

    data class Delta(
        val role: String? = null,
        val content: String? = null,
    )
}

data class ModelsResponse(
    val data: List<ModelInfo>,
) {
    data class ModelInfo(
        val id: String,
    )
}
