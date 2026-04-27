package com.kweaver.dip.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val userId: String? = null
)
