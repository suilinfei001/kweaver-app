package com.kweaver.dip.data.model

import com.google.gson.annotations.SerializedName

data class Skill(
    val name: String,
    val description: String? = null,
    val type: String? = null,
    val enabled: Boolean = true
)

data class SkillTreeItem(
    val name: String,
    val path: String,
    val type: String,
    val children: List<SkillTreeItem>? = null
)

data class SkillContent(
    val name: String,
    val path: String,
    val content: String
)

data class InstallSkillResult(
    val name: String,
    @SerializedName("skillPath")
    val skillPath: String
)

data class DigitalHumanAgentSkill(
    val name: String,
    val enabled: Boolean = true
)
