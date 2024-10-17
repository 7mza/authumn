package com.authumn.authumn.privileges

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasAuthority('admin')")
class PrivilegeCtrl
    @Autowired
    constructor(
        private val service: IPrivilegeService,
    ) : IPrivilegeApi {
        override fun save(t: PrivilegePostDto): PrivilegeGetDto = service.save(t).toDto()

        override fun findById(id: String): PrivilegeGetDto = service.findById(id).toDto()

        override fun findAll(): Collection<PrivilegeGetDto> = service.findAll().map { it.toDto() }

        override fun update(
            id: String,
            x: PrivilegePutDto,
        ): PrivilegeGetDto = service.update(id, x).toDto()

        override fun deleteById(id: String) = service.deleteById(id)
    }
