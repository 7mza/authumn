package com.authumn.authumn.keypairs

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasAuthority('admin')")
class KeyPairPairCtrl
    @Autowired
    constructor(
        private val service: IKeyPairService,
        private val textEncryptor: TextEncryptor,
    ) : IKeyPairApi {
        override fun generate(): KeyPairGetDto = service.save(service.generateJWKSet()).toDto(textEncryptor)

        override fun findById(id: String): KeyPairGetDto = service.findById(id).toDto(textEncryptor)

        override fun findAll(): Collection<KeyPairGetDto> = service.findAllByOrderByCreatedAtDesc().map { it.toDto(textEncryptor) }

        override fun deleteById(id: String) = service.deleteById(id)

        override fun deleteAllButNewest() = service.deleteAllButNewest()
    }
