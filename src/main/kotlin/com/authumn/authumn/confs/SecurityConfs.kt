package com.authumn.authumn.confs

import com.authumn.authumn.keypairs.IKeyPairService
import com.authumn.authumn.keypairs.PrivateKeyConverter
import com.authumn.authumn.keypairs.PublicKeyConverter
import com.authumn.authumn.users.CustomUserDetailsService
import com.authumn.authumn.users.IUserService
import com.authumn.authumn.users.KustomUser
import com.authumn.authumn.users.KustomUserMixin
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableScheduling
class SecurityConfs {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val idForEncode = "bcrypt"
        val encoders: MutableMap<String, PasswordEncoder> = mutableMapOf()
        encoders[idForEncode] = BCryptPasswordEncoder()
        encoders["scrypt"] = SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8()
        encoders["pbkdf2"] = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()
        encoders["argon2"] = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
        return DelegatingPasswordEncoder(idForEncode, encoders)
    }

    @Bean
    @Order(1)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java).oidc(Customizer.withDefaults())
        http
            .exceptionHandling {
                it.defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("/login"),
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML),
                )
            }.oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
        return http.build()
    }

    @Bean
    @Order(2)
    fun defaultSecurityFilterChain(
        http: HttpSecurity,
        securityProperties: SecurityProperties,
    ): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it
                    .requestMatchers(*securityProperties.permitAll?.toTypedArray() ?: emptyArray())
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/user")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.formLogin(Customizer.withDefaults())
            .formLogin { it.loginPage("/login") }
            .logout(Customizer.withDefaults())
            .logout { it.logoutUrl("/logout").logoutSuccessUrl("/login?logout") }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
            .csrf {
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                it.ignoringRequestMatchers(
                    AntPathRequestMatcher("/api/user", HttpMethod.POST.name()),
                )
            }.cors { it.configurationSource(urlBasedCorsConfigurationSource(securityProperties.permitOrigins)) }
        return http.build()
    }

    private fun urlBasedCorsConfigurationSource(origins: List<String>?): UrlBasedCorsConfigurationSource =
        UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration(
                "/**",
                CorsConfiguration().apply {
                    allowedOriginPatterns = origins
                    setAllowedMethods(listOf("*"))
                    allowedHeaders = listOf("*")
                    exposedHeaders = listOf("*")
                },
            )
        }

    // @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings =
        AuthorizationServerSettings.builder().multipleIssuersAllowed(true).build()

    @Bean
    fun userDetailsService(userService: IUserService): UserDetailsService = CustomUserDetailsService(userService)

    @Bean
    fun authenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder,
    ): DaoAuthenticationProvider =
        DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService)
            setPasswordEncoder(passwordEncoder)
        }

    // @Bean
    fun registeredClientRepository(template: JdbcTemplate): RegisteredClientRepository = JdbcRegisteredClientRepository(template)

    @Bean
    fun jdbcOAuth2AuthorizationConsentService(
        jdbcOperations: JdbcOperations,
        repository: RegisteredClientRepository,
    ): JdbcOAuth2AuthorizationConsentService = JdbcOAuth2AuthorizationConsentService(jdbcOperations, repository)

    // @Bean
    fun simpleJdbcOAuth2AuthorizationService(
        jdbcOperations: JdbcOperations,
        repository: RegisteredClientRepository,
    ): JdbcOAuth2AuthorizationService = JdbcOAuth2AuthorizationService(jdbcOperations, repository)

    @Bean
    fun jdbcOAuth2AuthorizationService(
        jdbcOperations: JdbcOperations,
        repository: RegisteredClientRepository,
    ): JdbcOAuth2AuthorizationService {
        val oAuth2AuthorizationService = JdbcOAuth2AuthorizationService(jdbcOperations, repository)
        val rowMapper = JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(repository)
        rowMapper.setObjectMapper(
            ObjectMapper().apply {
                registerKotlinModule()
                registerModule(JavaTimeModule())
                // disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                registerModules(SecurityJackson2Modules.getModules(javaClass.classLoader))
                registerModule(OAuth2AuthorizationServerJackson2Module())
                addMixIn(KustomUser::class.java, KustomUserMixin::class.java)
            },
        )
        oAuth2AuthorizationService.setAuthorizationRowMapper(rowMapper)
        return oAuth2AuthorizationService
    }

    @Bean
    fun jwtEncoder(jwkSource: JWKSource<SecurityContext>) = NimbusJwtEncoder(jwkSource)

    @Component
    class CustomJWKSource
        @Autowired
        constructor(
            private val service: IKeyPairService,
            private val publicKeyConverter: PublicKeyConverter,
            private val privateKeyConverter: PrivateKeyConverter,
        ) : JWKSource<SecurityContext> {
            override fun get(
                jwkSelector: JWKSelector,
                context: SecurityContext?,
            ): List<JWK> {
                val pairs =
                    service.findAllByOrderByCreatedAtDesc().map {
                        it.toRSAKey(
                            publicKeyConverter = publicKeyConverter,
                            privateKeyConverter = privateKeyConverter,
                        )
                    }
                return pairs
                    .map {
                        RSAKey
                            .Builder(it.toRSAPublicKey())
                            .privateKey(it.toRSAPrivateKey())
                            .keyID(it.keyID)
                            .build()
                    }.filter {
                        jwkSelector.matcher.matches(it)
                    }
            }
        }

    @Component
    class ScheduledTasks
        @Autowired
        constructor(
            private val service: IKeyPairService,
        ) {
            private val lock = Any()

            @Scheduled(cron = "\${scheduled.rotate}")
            fun rotateKeys() {
                synchronized(lock) {
                    logger.debug("Scheduled task: generating new key pair")
                    service.save(service.generateJWKSet())
                }
            }
        }

    @Configuration
    @ConfigurationProperties(prefix = "spring.security")
    data class SecurityProperties(
        var permitAll: List<String>?,
        var permitOrigins: List<String>?,
    )
}
