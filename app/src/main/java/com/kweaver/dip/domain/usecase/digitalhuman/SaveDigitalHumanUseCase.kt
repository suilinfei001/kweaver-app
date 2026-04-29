package com.kweaver.dip.domain.usecase.digitalhuman

import com.kweaver.dip.data.model.*
import com.kweaver.dip.data.repository.DigitalHumanRepository
import javax.inject.Inject

class SaveDigitalHumanUseCase @Inject constructor(
    private val digitalHumanRepository: DigitalHumanRepository
) {
    suspend operator fun invoke(
        id: String?,
        name: String,
        creature: String,
        soul: String,
        bknEntries: List<BknEntry>,
        selectedSkills: List<String>,
        channelType: String,
        channelAppId: String,
        channelAppSecret: String
    ): Result<Any> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name is required"))
        }

        val bkn = bknEntries.filter { it.name.isNotBlank() }.ifEmpty { null }
        val skills = selectedSkills.ifEmpty { null }
        val channel = if (channelAppId.isNotBlank()) {
            ChannelConfig(
                type = channelType.ifBlank { null },
                appId = channelAppId,
                appSecret = channelAppSecret
            )
        } else null

        return if (id != null) {
            digitalHumanRepository.updateDigitalHuman(
                id,
                UpdateDigitalHumanRequest(
                    name = name,
                    creature = creature.ifBlank { null },
                    soul = soul.ifBlank { null },
                    bkn = bkn,
                    skills = skills,
                    channel = channel
                )
            )
        } else {
            digitalHumanRepository.createDigitalHuman(
                CreateDigitalHumanRequest(
                    name = name,
                    creature = creature.ifBlank { null },
                    soul = soul.ifBlank { null },
                    bkn = bkn,
                    skills = skills,
                    channel = channel
                )
            )
        }
    }
}
