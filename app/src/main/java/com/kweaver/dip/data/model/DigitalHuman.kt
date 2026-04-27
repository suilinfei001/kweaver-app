package com.kweaver.dip.data.model

import com.google.gson.annotations.SerializedName

data class DigitalHuman(
    val id: String,
    val name: String,
    val creature: String? = null,
    @SerializedName("icon_id")
    val iconId: String? = null
)

data class DigitalHumanDetail(
    val id: String,
    val name: String,
    val creature: String? = null,
    @SerializedName("icon_id")
    val iconId: String? = null,
    val soul: String? = null,
    val bkn: List<BknEntry>? = null,
    val skills: List<String>? = null,
    val channel: ChannelConfig? = null
)

data class CreateDigitalHumanRequest(
    val id: String? = null,
    val name: String,
    val creature: String? = null,
    @SerializedName("icon_id")
    val iconId: String? = null,
    val soul: String? = null,
    val skills: List<String>? = null,
    val bkn: List<BknEntry>? = null,
    val channel: ChannelConfig? = null
)

data class UpdateDigitalHumanRequest(
    val name: String? = null,
    val creature: String? = null,
    @SerializedName("icon_id")
    val iconId: String? = null,
    val soul: String? = null,
    val skills: List<String>? = null,
    val bkn: List<BknEntry>? = null,
    val channel: ChannelConfig? = null
)

data class CreateDigitalHumanResult(
    val id: String,
    val name: String
)

data class BknEntry(
    val name: String,
    val url: String
)

data class ChannelConfig(
    val type: String? = null,
    @SerializedName("appId")
    val appId: String? = null,
    @SerializedName("appSecret")
    val appSecret: String? = null
)

data class BuiltInDigitalHuman(
    val id: String,
    val name: String,
    val creature: String? = null,
    @SerializedName("is_created")
    val isCreated: Boolean = false
)
