package com.jwtuppg.security

sealed class AuthState {
    data class Authenticated(val token: String, val aesKey: String) : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

