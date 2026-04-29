package com.kweaver.dip.domain.usecase.digitalhuman

import com.kweaver.dip.data.model.DigitalHuman
import com.kweaver.dip.data.repository.DigitalHumanRepository
import javax.inject.Inject

class ListDigitalHumansUseCase @Inject constructor(
    private val repository: DigitalHumanRepository
) {
    suspend operator fun invoke(): Result<List<DigitalHuman>> =
        repository.listDigitalHumans()
}
