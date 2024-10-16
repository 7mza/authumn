package com.authumn.authumn.commons

data class ErrorDto(
    val code: String,
    val message: String?,
    val path: String,
    val timestamp: String,
    val requestId: String,
)
