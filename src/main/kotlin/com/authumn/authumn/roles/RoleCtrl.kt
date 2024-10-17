package com.authumn.authumn.roles

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasAuthority('admin')")
class RoleCtrl
    @Autowired
    constructor(
        private val service: IRoleService,
    ) : IRoleApi {
        override fun save(t: RolePostDto): RoleGetDto = service.save(t).toDto()

        override fun findById(id: String): RoleGetDto = service.findById(id).toDto()

        override fun findAll(): Collection<RoleGetDto> = service.findAll().map { it.toDto() }

        override fun update(
            id: String,
            x: RolePutDto,
        ): RoleGetDto = service.update(id, x).toDto()

        override fun deleteById(id: String) = service.deleteById(id)
    }
