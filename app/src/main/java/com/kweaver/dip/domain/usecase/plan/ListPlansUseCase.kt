package com.kweaver.dip.domain.usecase.plan

import com.kweaver.dip.data.model.CronJob
import com.kweaver.dip.data.repository.PlanRepository
import javax.inject.Inject

class ListPlansUseCase @Inject constructor(
    private val repository: PlanRepository
) {
    suspend operator fun invoke(): Result<List<CronJob>> =
        repository.listPlans()
}
