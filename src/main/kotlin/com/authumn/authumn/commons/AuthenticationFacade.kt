package com.authumn.authumn.commons

import com.authumn.authumn.users.KustomUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

interface IAuthenticationFacade {
    fun getAuthentication(): Authentication?
}

@Component
class AuthenticationFacade : IAuthenticationFacade {
    override fun getAuthentication(): Authentication? = SecurityContextHolder.getContext()?.authentication

    fun getPrincipal(): KustomUser? = this.getAuthentication()?.principal as KustomUser
}
