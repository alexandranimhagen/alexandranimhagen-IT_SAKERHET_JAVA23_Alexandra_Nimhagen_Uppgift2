package com.jwtuppg.messages

data class Message(
    val id: String = "",
    val user: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)

