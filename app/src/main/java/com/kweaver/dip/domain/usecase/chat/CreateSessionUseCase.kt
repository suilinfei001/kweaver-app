package com.kweaver.dip.domain.usecase.chat

import com.kweaver.dip.data.repository.ChatRepository
import javax.inject.Inject

class CreateSessionUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(agentId: String): Result<String> =
        chatRepository.createSessionKey(agentId)
}
