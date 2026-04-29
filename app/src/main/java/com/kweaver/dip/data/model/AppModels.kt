package com.kweaver.dip.data.model

import com.google.gson.annotations.SerializedName

data class ApplicationInfo(
    val id: Int,
    val key: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val category: String? = null,
    val version: String? = null,
    @SerializedName("is_config")
    val isConfig: Boolean = false,
    @SerializedName("updated_by")
    val updatedBy: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("micro_app")
    val microApp: MicroAppInfo? = null,
    val pinned: Boolean = false,
    @SerializedName("isBuiltIn")
    val isBuiltIn: Boolean = false
)

data class MicroAppInfo(
    val name: String,
    val entry: String,
    val headless: Boolean = false
)

data class PinAppRequest(
    val pinned: Boolean
)
