package com.kweaver.dip.data.api

import com.kweaver.dip.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface DipStudioApi {

    // --- Digital Human ---

    @GET("api/dip-studio/v1/digital-human")
    suspend fun listDigitalHumans(): List<DigitalHuman>

    @GET("api/dip-studio/v1/digital-human/built-in")
    suspend fun listBuiltInDigitalHumans(): List<BuiltInDigitalHuman>

    @PUT("api/dip-studio/v1/digital-human/built-in/{ids}")
    suspend fun createBuiltInDigitalHumans(@Path("ids") ids: String): List<CreateDigitalHumanResult>

    @POST("api/dip-studio/v1/digital-human")
    suspend fun createDigitalHuman(@Body request: CreateDigitalHumanRequest): CreateDigitalHumanResult

    @GET("api/dip-studio/v1/digital-human/{id}")
    suspend fun getDigitalHuman(@Path("id") id: String): DigitalHumanDetail

    @PUT("api/dip-studio/v1/digital-human/{id}")
    suspend fun updateDigitalHuman(
        @Path("id") id: String,
        @Body request: UpdateDigitalHumanRequest
    ): DigitalHumanDetail

    @DELETE("api/dip-studio/v1/digital-human/{id}")
    suspend fun deleteDigitalHuman(
        @Path("id") id: String,
        @Query("deleteFiles") deleteFiles: Boolean? = null
    ): Response<Unit>

    // --- Skills ---

    @GET("api/dip-studio/v1/skills")
    suspend fun listSkills(@Query("name") search: String? = null): List<Skill>

    @GET("api/dip-studio/v1/skills/{name}/tree")
    suspend fun getSkillTree(@Path("name") name: String): List<SkillTreeItem>

    @GET("api/dip-studio/v1/skills/{name}/content")
    suspend fun getSkillContent(
        @Path("name") name: String,
        @Query("path") path: String = "SKILL.md"
    ): Map<String, String>

    @Multipart
    @POST("api/dip-studio/v1/skills/install")
    suspend fun installSkill(
        @Part file: MultipartBody.Part
    ): InstallSkillResult

    @DELETE("api/dip-studio/v1/skills/{name}")
    suspend fun uninstallSkill(@Path("name") name: String): Map<String, String>

    @GET("api/dip-studio/v1/digital-human/{id}/skills")
    suspend fun listDigitalHumanSkills(@Path("id") id: String): List<DigitalHumanAgentSkill>

    // --- Sessions ---

    @GET("api/dip-studio/v1/sessions")
    suspend fun listSessions(
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null,
        @Query("agentId") agentId: String? = null
    ): List<Session>

    @GET("api/dip-studio/v1/sessions/{key}")
    suspend fun getSessionSummary(@Path("key") key: String): SessionSummary

    @GET("api/dip-studio/v1/sessions/{key}/messages")
    suspend fun getSessionMessages(
        @Path("key") key: String,
        @Query("limit") limit: Int? = null
    ): List<SessionMessage>

    @DELETE("api/dip-studio/v1/sessions/{key}")
    suspend fun deleteSession(@Path("key") key: String): Response<Unit>

    @GET("api/dip-studio/v1/digital-human/{id}/sessions")
    suspend fun listDigitalHumanSessions(
        @Path("id") id: String,
        @Query("limit") limit: Int? = null,
        @Query("search") search: String? = null
    ): List<Session>

    // --- Chat ---

    @POST("api/dip-studio/v1/chat/session")
    suspend fun createSessionKey(
        @Header("x-user-id") userId: String,
        @Body request: CreateSessionKeyRequest
    ): CreateSessionKeyResponse

    @POST("api/dip-studio/v1/chat/agent")
    suspend fun sendChatMessage(
        @Header("x-openclaw-session-key") sessionKey: String,
        @Body body: RequestBody
    ): ResponseBody

    @Multipart
    @POST("api/dip-studio/v1/chat/upload")
    suspend fun uploadChatFile(
        @Header("x-openclaw-session-key") sessionKey: String,
        @Part file: MultipartBody.Part
    ): ChatUploadResult

    // --- Plans ---

    @GET("api/dip-studio/v1/plans")
    suspend fun listPlans(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("enabled") enabled: Boolean? = null
    ): List<CronJob>

    @GET("api/dip-studio/v1/plans/{id}")
    suspend fun getPlan(@Path("id") id: String): CronJob

    @GET("api/dip-studio/v1/plans/{id}/content")
    suspend fun getPlanContent(@Path("id") id: String): PlanContent

    @GET("api/dip-studio/v1/plans/{id}/runs")
    suspend fun listPlanRuns(
        @Path("id") id: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): List<CronRunEntry>

    @PUT("api/dip-studio/v1/plans/{id}")
    suspend fun updatePlan(
        @Path("id") id: String,
        @Body request: UpdatePlanRequest
    ): CronJob

    @DELETE("api/dip-studio/v1/plans/{id}")
    suspend fun deletePlan(@Path("id") id: String): Response<Unit>

    @GET("api/dip-studio/v1/digital-human/{id}/plans")
    suspend fun listDigitalHumanPlans(
        @Path("id") id: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): List<CronJob>

    // --- Guide ---

    @GET("api/dip-studio/v1/guide/status")
    suspend fun getGuideStatus(): GuideStatus

    @GET("api/dip-studio/v1/guide/openclaw-config")
    suspend fun getOpenClawConfig(): OpenClawConfig

    @POST("api/dip-studio/v1/guide/initialize")
    suspend fun initializeGuide(@Body request: InitializeGuideRequest): Response<Unit>
}
