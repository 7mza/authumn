package com.authumn.authumn.commons

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

interface IAuthenticationFacade {
    val authentication: Authentication?
}

@Component
class AuthenticationFacade : IAuthenticationFacade {
    override val authentication: Authentication?
        get() = SecurityContextHolder.getContext()?.authentication
}
