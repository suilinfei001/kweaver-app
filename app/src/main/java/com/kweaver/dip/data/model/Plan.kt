package com.kweaver.dip.data.model

import com.google.gson.annotations.SerializedName

data class CronJob(
    val id: String,
    @SerializedName("agentId")
    val agentId: String,
    @SerializedName("sessionKey")
    val sessionKey: String,
    val name: String,
    val enabled: Boolean,
    @SerializedName("createdAtMs")
    val createdAtMs: Long,
    @SerializedName("updatedAtMs")
    val updatedAtMs: Long,
    val schedule: CronSchedule,
    val state: CronJobState? = null
)

data class CronSchedule(
    val kind: String? = null,
    val at: String? = null,
    @SerializedName("everyMs")
    val everyMs: Long? = null,
    @SerializedName("anchorMs")
    val anchorMs: Long? = null,
    val expr: String? = null,
    val tz: String? = null
)

data class CronJobState(
    val lastRunAtMs: Long? = null,
    val nextRunAtMs: Long? = null,
    val status: String? = null
)

data class CronRunEntry(
    val ts: Long,
    @SerializedName("jobId")
    val jobId: String,
    val action: String,
    val status: String,
    val error: String? = null,
    val summary: String? = null,
    @SerializedName("durationMs")
    val durationMs: Long? = null
)

data class UpdatePlanRequest(
    val name: String? = null,
    val enabled: Boolean? = null
)

data class PlanContent(
    val content: String
)
