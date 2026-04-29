package com.kweaver.dip.domain.usecase.session

import com.kweaver.dip.data.model.Session
import com.kweaver.dip.data.repository.SessionRepository
import javax.inject.Inject

class ListSessionsUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(
        search: String? = null,
        agentId: String? = null
    ): Result<List<Session>> =
        repository.listSessions(search, agentId)
}
