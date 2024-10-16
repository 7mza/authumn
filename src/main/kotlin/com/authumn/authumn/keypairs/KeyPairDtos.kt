package com.authumn.authumn.keypairs

data class KeyPairGetDto(
    val id: String,
    val publicKey: String,
    val privateKey: String,
    // @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val createdAt: String,
)
