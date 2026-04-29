package com.kweaver.dip.domain.usecase.digitalhuman

import com.kweaver.dip.data.repository.DigitalHumanRepository
import javax.inject.Inject

class DeleteDigitalHumanUseCase @Inject constructor(
    private val repository: DigitalHumanRepository
) {
    suspend operator fun invoke(id: String, deleteFiles: Boolean = false): Result<Unit> =
        repository.deleteDigitalHuman(id, deleteFiles)
}
