package com.authumn.authumn.privileges

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class PrivilegeCtrl
    @Autowired
    constructor(
        private val service: IPrivilegeService,
    ) : IPrivilegeApi {
        @PreAuthorize("hasAuthority('admin')")
        override fun save(t: PrivilegePostDto): PrivilegeGetDto = service.save(t).toDto()

        @PreAuthorize("hasAuthority('admin')")
        override fun findById(id: String): PrivilegeGetDto = service.findById(id).toDto()

        @PreAuthorize("hasAuthority('admin')")
        override fun findAll(): Collection<PrivilegeGetDto> = service.findAll().map { it.toDto() }

        @PreAuthorize("hasAuthority('admin')")
        override fun update(
            id: String,
            x: PrivilegePutDto,
        ): PrivilegeGetDto = service.update(id, x).toDto()

        @PreAuthorize("hasAuthority('admin')")
        override fun deleteById(id: String) = service.deleteById(id)
    }
