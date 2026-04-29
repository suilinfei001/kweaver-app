package com.kweaver.dip.domain.usecase.auth

import com.kweaver.dip.data.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(serverUrl: String, username: String, password: String): Result<String> =
        authRepository.login(serverUrl, username, password)

    suspend fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    suspend fun logout() = authRepository.logout()
}
