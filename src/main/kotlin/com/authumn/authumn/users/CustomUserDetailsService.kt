package com.authumn.authumn.users

import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
@Primary
class CustomUserDetailsService
    @Autowired
    constructor(
        private val userService: IUserService,
    ) : UserDetailsService {
        override fun loadUserByUsername(username: String): UserDetails {
            try {
                val user = userService.findByEmail(username)
                return KustomUser(
                    id = user.id,
                    username = user.email,
                    password = user.password,
                    authorities = getAuthorities(user),
                )
            } catch (_: CustomResourceNotFoundException) {
                throw UsernameNotFoundException("User not found")
            }
        }

        private fun getAuthorities(user: User): Collection<GrantedAuthority> =
            user.roles.map { SimpleGrantedAuthority(it.label) } +
                user.roles.flatMap {
                    it.privileges.map { it2 ->
                        SimpleGrantedAuthority(it2.label)
                    }
                }
    }

class KustomUser : org.springframework.security.core.userdetails.User {
    var id: String

    constructor(id: String, username: String, password: String, authorities: Collection<GrantedAuthority>) : super(
        username,
        password,
        authorities,
    ) {
        this.id = id
    }

    constructor(
        id: String,
        username: String,
        password: String,
        enabled: Boolean,
        accountNonExpired: Boolean,
        credentialsNonExpired: Boolean,
        accountNonLocked: Boolean,
        authorities: Collection<GrantedAuthority>,
    ) : super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities) {
        this.id = id
    }
}

abstract class KustomUserMixin(
    @JsonProperty("id") id: String,
    @JsonProperty("username") username: String,
    @JsonProperty("password") password: String,
    @JsonProperty("authorities") authorities: Collection<GrantedAuthority>,
)
