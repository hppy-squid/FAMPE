package com.FAMPE.fampe.model

data class GlobalSession(
    val sessionId: String = "",
    val sessionLength: Int = 24,
    val sessionStart: Long = 0
)
