package com.authumn.authumn.confs

import com.authumn.authumn.commons.Commons
import com.authumn.authumn.keypairs.IKeyPairService
import com.authumn.authumn.privileges.IPrivilegeService
import com.authumn.authumn.privileges.PrivilegePostDto
import com.authumn.authumn.roles.IRoleService
import com.authumn.authumn.roles.RolePostDto
import com.authumn.authumn.users.IUserService
import com.authumn.authumn.users.User
import com.authumn.authumn.users.UserPostDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Instant

@Configuration
class InitConfs {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun applicationReadyListener(
        keyPairService: IKeyPairService,
        userService: IUserService,
        publisher: ApplicationEventPublisher,
    ): ApplicationListener<ApplicationReadyEvent> =
        ApplicationListener {
            if (keyPairService.count() == 0L) {
                logger.debug("no keys found in db, publishing new KeyPairGenerationEvent")
                publisher.publishEvent(KeyPairGenerationEvent())
            }
            if (userService.count() == 0L) {
                logger.debug("no users found in db, publishing new InitEvent")
                publisher.publishEvent(InitEvent())
            }
        }

    @Bean
    fun keyPairGenerationRequestListener(service: IKeyPairService): ApplicationListener<KeyPairGenerationEvent> =
        ApplicationListener {
            logger.debug("KeyPairGenerationEvent: generating new key pair")
            service.save(service.generateJWKSet())
        }

    @Profile("init")
    @Bean
    fun userGenerationRequestListener(
        initProps: InitProperties,
        privilegeService: IPrivilegeService,
        roleService: IRoleService,
        userService: IUserService,
        objectMapper: ObjectMapper,
    ): ApplicationListener<InitEvent> =
        ApplicationListener {
            logger.debug("InitEvent: generating init users/roles/privileges")
            val users =
                init(
                    initProps = initProps,
                    privilegeService = privilegeService,
                    roleService = roleService,
                    userService = userService,
                )
            users.forEach {
                logger.debug("init new user: {}", Commons.writeJson(it.toDto(), objectMapper))
            }
        }

    class KeyPairGenerationEvent(
        val instant: Instant = Instant.now(),
    ) : ApplicationEvent(instant) {
        override fun getSource() = instant
    }

    class InitEvent(
        val instant: Instant = Instant.now(),
    ) : ApplicationEvent(instant) {
        override fun getSource() = instant
    }

    @Configuration
    @ConfigurationProperties(prefix = "init")
    data class InitProperties(
        var privileges: List<PrivilegeProp>?,
        var roles: List<RoleProp>?,
        var users: List<UserProp>?,
    ) {
        data class PrivilegeProp(
            var label: String?,
            var isDefault: Boolean?,
        )

        data class RoleProp(
            var label: String?,
            var isDefault: Boolean?,
            var privileges: List<String>?,
        )

        data class UserProp(
            var email: String?,
            var password: String?,
            var roles: List<String>?,
        )
    }

    private fun init(
        initProps: InitProperties,
        privilegeService: IPrivilegeService,
        roleService: IRoleService,
        userService: IUserService,
    ): Collection<User> {
        val privileges =
            privilegeService.saveMany(
                initProps.privileges?.map {
                    PrivilegePostDto(
                        label = it.label!!,
                        isDefault = it.isDefault!!,
                    )
                }!!,
            )

        val roles =
            roleService.saveMany(
                initProps.roles?.map { it1 ->
                    RolePostDto(
                        label = it1.label!!,
                        isDefault = it1.isDefault!!,
                        privileges = privileges.filter { it2 -> it1.privileges?.contains(it2.label) ?: false }.map { it.id },
                    )
                }!!,
            )

        return userService.saveMany(
            initProps.users
                ?.map { it1 ->
                    val password =
                        it1.password?.let {
                            it.ifBlank { RandomStringUtils.randomAlphabetic(10) }
                        } ?: RandomStringUtils.randomAlphabetic(10)
                    UserPostDto(
                        email = it1.email!!,
                        password = password,
                        roles = roles.filter { it2 -> it1.roles?.contains(it2.label) ?: false }.map { it.id },
                    )
                }!!
                .onEach {
                    logger.debug("user: ${it.email}, password: ${it.password}")
                },
        )
    }
}
