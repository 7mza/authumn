package com.authumn.authumn.commons

import java.time.Instant

data class CustomResourceNotFoundException(
    override val message: String,
    val instant: Instant = Instant.now(),
) : RuntimeException(message)

data class CustomConstraintViolationException(
    override val message: String,
    val instant: Instant = Instant.now(),
) : RuntimeException(message)
