package com.kweaver.dip.data.repository

import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.model.*
import javax.inject.Inject

class SkillRepository @Inject constructor(
    private val api: DipStudioApi,
    private val tokenDataStore: TokenDataStore
) {
    suspend fun listSkills(search: String? = null): Result<List<Skill>> = try {
        Result.success(api.listSkills(search))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSkillTree(name: String): Result<List<SkillTreeItem>> = try {
        Result.success(api.getSkillTree(name))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSkillContent(name: String, path: String = "SKILL.md"): Result<String> = try {
        val result = api.getSkillContent(name, path)
        Result.success(result["content"] ?: "")
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun installSkill(fileName: String, fileBytes: ByteArray): Result<InstallSkillResult> = try {
        val requestBody = okhttp3.RequestBody.create(
            okhttp3.MediaType.parse("application/zip"), fileBytes
        )
        val multipartBody = okhttp3.MultipartBody.Part.createFormData("file", fileName, requestBody)
        Result.success(api.installSkill(multipartBody))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uninstallSkill(name: String): Result<Unit> = try {
        api.uninstallSkill(name)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun listDigitalHumanSkills(agentId: String): Result<List<DigitalHumanAgentSkill>> = try {
        Result.success(api.listDigitalHumanSkills(agentId))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
