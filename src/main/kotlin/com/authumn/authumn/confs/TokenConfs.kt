package com.authumn.authumn.confs

import com.authumn.authumn.keypairs.IKeyPairService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2Token
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationToken
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationToken
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component

@Configuration
class TokenConfs {
    @Bean
    fun delegatingOAuth2TokenGenerator(
        encoder: JwtEncoder,
        customizer: OAuth2TokenCustomizer<JwtEncodingContext>,
    ): OAuth2TokenGenerator<OAuth2Token> {
        val generator = JwtGenerator(encoder)
        generator.setJwtCustomizer(customizer)
        return DelegatingOAuth2TokenGenerator(
            generator,
            OAuth2AccessTokenGenerator(),
            OAuth2RefreshTokenGenerator(),
        )
    }

    @Bean
    fun simpleJwtAuthenticationConverter(): JwtAuthenticationConverter =
        JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(
                JwtGrantedAuthoritiesConverter().apply {
                    setAuthoritiesClaimName("scope")
                    setAuthorityPrefix("")
                },
            )
        }

    // @Bean
    fun customJwtAuthenticationConverter(): JwtAuthenticationConverter =
        JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(KustomJwtGrantedAuthoritiesConverter())
        }

    @Component
    class CustomOAuth2TokenCustomizer
        @Autowired
        constructor(
            private val service: IKeyPairService,
        ) : OAuth2TokenCustomizer<JwtEncodingContext> {
            override fun customize(context: JwtEncodingContext) {
                context.jwsHeader.keyId(service.findNewest().id)
                if (OAuth2TokenType.ACCESS_TOKEN == context.tokenType) {
                    when (context.getAuthorizationGrant<Authentication>()) {
                        is OAuth2ClientCredentialsAuthenticationToken -> {
                            context.claims.claims { claims ->
                                // placing client-credentials scope claim in role claim
                                claims["roles"] = (claims["scope"] as Iterable<*>).map { it.toString() }.toSet()
                            }
                        }
                        is OAuth2AuthorizationCodeAuthenticationToken -> {
                            context.claims.claims { claims ->
                                val roles: Set<String> =
                                    AuthorityUtils
                                        .authorityListToSet(context.getPrincipal<Authentication>().authorities)
                                        .map { it.replaceFirst("ROLE_", "") }
                                        .toSet()
                                // adding current granted authorities in client-authorization role claim
                                claims["roles"] = roles
                                // adding previous role claim in client-authorization scope claim
                                claims["scope"] = (claims["scope"] as Iterable<*>).map { it.toString() }.plus(roles).toSet()
                                /*
                                 * FIXME:
                                 * scope of a token defines what a resource-owner allowed an OAuth2 client to do on his behalf
                                 * roles of a resource-owner himself defines what they are allowed to do on resource servers
                                 * https://github.com/ch4mpy/spring-addons/tree/master/samples/tutorials/reactive-resource-server
                                 *
                                 * current GrantedAuthorities will be mapped from CustomUserDetailsService
                                 */
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        }
}

class KustomJwtGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(source: Jwt): Collection<GrantedAuthority> =
        (source.claims["scope"] as Iterable<*>).map { SimpleGrantedAuthority(it.toString()) }

    override fun <U> andThen(after: Converter<in Collection<GrantedAuthority>, out U>): Converter<Jwt, U> = super.andThen(after)
}
