package com.authumn.authumn.roles

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class RoleCtrl
    @Autowired
    constructor(
        private val service: IRoleService,
    ) : IRoleApi {
        @PreAuthorize("hasAuthority('admin')")
        override fun save(t: RolePostDto): RoleGetDto = service.save(t).toDto()

        @PreAuthorize("hasAuthority('admin')")
        override fun findById(id: String): RoleGetDto = service.findById(id).toDto()

        @PreAuthorize("hasAuthority('admin')")
        override fun findAll(): Collection<RoleGetDto> = service.findAll().map { it.toDto() }

        @PreAuthorize("hasAuthority('admin')")
        override fun update(
            id: String,
            x: RolePutDto,
        ): RoleGetDto = service.update(id, x).toDto()

        @PreAuthorize("hasAuthority('admin')")
        override fun deleteById(id: String) = service.deleteById(id)
    }
