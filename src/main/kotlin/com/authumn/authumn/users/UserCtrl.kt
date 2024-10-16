package com.authumn.authumn.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController

@RestController
class UserCtrl
    @Autowired
    constructor(
        private val service: IUserService,
    ) : IUserApi {
        override fun save(t: UserPostDto): UserGetDto = service.save(t).toDto()

        @PreAuthorize("hasAuthority('admin') or (hasAuthority('user') and #id == principal?.id)")
        override fun findById(id: String): UserGetDto = service.findById(id).toDto()

        @PreAuthorize("hasAuthority('admin')")
        override fun findAll(): Collection<UserGetDto> =
            service.findAll().map {
                it.toDto()
            }

        @PreAuthorize("hasAuthority('admin') or (hasAuthority('user') and #id == principal?.id)")
        override fun update(
            id: String,
            x: UserPutDto,
        ): UserGetDto = service.update(id, x).toDto()

        @PreAuthorize("hasAuthority('admin') or (hasAuthority('user') and #id == principal?.id)")
        override fun deleteById(id: String) = service.deleteById(id)
    }
