package com.authumn.authumn.keypairs

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.web.bind.annotation.RestController

@RestController
class KeyPairPairCtrl
    @Autowired
    constructor(
        private val service: IKeyPairService,
        private val textEncryptor: TextEncryptor,
    ) : IKeyPairApi {
        @PreAuthorize("hasAuthority('admin')")
        override fun generate(): KeyPairGetDto = service.save(service.generateJWKSet()).toDto(textEncryptor)

        @PreAuthorize("hasAuthority('admin')")
        override fun findById(id: String): KeyPairGetDto = service.findById(id).toDto(textEncryptor)

        @PreAuthorize("hasAuthority('admin')")
        override fun findAll(): Collection<KeyPairGetDto> = service.findAllByOrderByCreatedAtDesc().map { it.toDto(textEncryptor) }

        @PreAuthorize("hasAuthority('admin')")
        override fun deleteById(id: String) = service.deleteById(id)

        @PreAuthorize("hasAuthority('admin')")
        override fun deleteAllButNewest() = service.deleteAllButNewest()
    }
