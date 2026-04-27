package com.kweaver.dip.data.model

data class GuideStatus(
    val state: String,
    val isInitialized: Boolean = false
)

data class OpenClawConfig(
    val address: String? = null,
    val hasToken: Boolean = false
)

data class InitializeGuideRequest(
    @SerializedName("openclaw_address")
    val openclawAddress: String,
    @SerializedName("openclaw_token")
    val openclawToken: String,
    @SerializedName("kweaver_base_url")
    val kweaverBaseUrl: String? = null,
    @SerializedName("kweaver_token")
    val kweaverToken: String? = null
)
