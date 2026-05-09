package com.kweaver.dip.data.model

data class AiConfig(
    val baseUrl: String,
    val modelId: String,
    val contextSize: Int = 4096,
    val apiKey: String,
)
