package com.kweaver.dip.data.model

data class AiConfig(
    val baseUrl: String,
    val modelId: String,
    val contextSize: Int = 4096,
    val apiKey: String,
    val asrUrl: String = "",
    val asrEnabled: Boolean = false,
    val ttsUrl: String = "",
    val ttsEnabled: Boolean = false,
)
