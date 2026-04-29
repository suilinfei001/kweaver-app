package com.kweaver.dip.domain.usecase.skill

import com.kweaver.dip.data.model.Skill
import com.kweaver.dip.data.repository.SkillRepository
import javax.inject.Inject

class ListSkillsUseCase @Inject constructor(
    private val repository: SkillRepository
) {
    suspend operator fun invoke(search: String? = null): Result<List<Skill>> =
        repository.listSkills(search)
}
