package com.sylovestp.firebasetest.testspringrestapp.model

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val username: String,
)