package com.kweaver.dip.domain.usecase.chat

import com.kweaver.dip.data.model.SseEvent
import com.kweaver.dip.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(
        sessionKey: String,
        message: String,
        attachments: List<Pair<String, String>>? = null
    ): Flow<SseEvent> =
        chatRepository.streamChat(sessionKey, message, attachments)
}
