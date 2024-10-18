package com.authumn.authumn.keypairs

data class KeyPairGetDto(
    val id: String,
    val publicKey: String,
    val privateKey: String,
    val createdAt: String,
    val updateAt: String,
)
