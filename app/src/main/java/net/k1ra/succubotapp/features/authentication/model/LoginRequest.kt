package net.k1ra.succubotapp.features.authentication.model

data class LoginRequest(
    val email: String,
    val password: String
)