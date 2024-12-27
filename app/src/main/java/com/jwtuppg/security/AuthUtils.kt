package com.jwtuppg.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Markera funktionen som suspend
suspend fun getUserIdAndEncryptionKeyFromToken(value: AuthState?): Pair<Int, String?> {
    return when (value) {
        is AuthState.Authenticated -> {
            val email = JwtUtil.getEmailFromToken(value.token)
            // Flytta databasoperationer till IO-tråden
            val userId = withContext(Dispatchers.IO) { DatabaseManager.getUserIdByEmail(email) ?: 0 }
            val encryptionKey = withContext(Dispatchers.IO) { DatabaseManager.getEncryptionKey(userId) }
            Pair(userId, encryptionKey)
        }
        else -> Pair(0, null)
    }
}
