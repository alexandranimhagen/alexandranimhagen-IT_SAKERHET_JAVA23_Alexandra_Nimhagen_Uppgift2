package com.jwtuppg.messages

data class Capsule(
    val id: Int,
    val userId: Int,
    val message: String,
    val timestamp: Long
)

