package com.kweaver.dip.domain.usecase.store

import com.kweaver.dip.data.model.ApplicationInfo
import com.kweaver.dip.data.repository.AppRepository
import javax.inject.Inject

class ListAppsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(): Result<List<ApplicationInfo>> =
        repository.listApplications()
}
