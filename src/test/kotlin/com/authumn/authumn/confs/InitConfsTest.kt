package com.authumn.authumn.confs

import com.authumn.authumn.keypairs.IKeyPairRepository
import com.authumn.authumn.keypairs.IKeyPairService
import com.authumn.authumn.privileges.IPrivilegeService
import com.authumn.authumn.roles.IRoleService
import com.authumn.authumn.users.IUserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KeyPairGenerationEventTest {
    @Autowired
    private lateinit var keyPairRepository: IKeyPairRepository

    @Autowired
    private lateinit var keyPairService: IKeyPairService

    @SpyBean
    private lateinit var initConfs: InitConfs

    @AfterEach
    fun afterEach() {
        keyPairRepository.deleteAll()
    }

    @Test
    fun `KeyPairGenerationEvent is published on application start`() {
        verify(initConfs, times(1)).keyPairGenerationRequestListener(any())
        assertThat(keyPairService.count()).isOne()
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["default", "init"])
class InitEventTest {
    @Autowired
    private lateinit var privilegeService: IPrivilegeService

    @Autowired
    private lateinit var roleService: IRoleService

    @Autowired
    private lateinit var userService: IUserService

    @SpyBean
    private lateinit var initConfs: InitConfs

    @AfterEach
    fun afterEach() {
        userService.deleteAll()
        roleService.deleteAll()
        privilegeService.deleteAll()
    }

    @Test
    fun `InitEventTest is published on application start`() {
        verify(initConfs, times(1)).userGenerationRequestListener(any(), any(), any(), any(), any())
        assertThat(userService.count()).isEqualTo(2)
        assertThat(privilegeService.count()).isEqualTo(2)
        assertThat(userService.count()).isEqualTo(2)
        val admin = userService.findAll().find { it.email == "admin@mail.com" }
        assertThat(admin).isNotNull
        assertThat(admin!!.roles.map { it.label }).hasSameElementsAs(listOf("user", "admin"))
        val user = userService.findAll().find { it.email == "user@mail.com" }
        assertThat(user).isNotNull
        assertThat(user!!.roles.map { it.label }).hasSameElementsAs(listOf("user"))
    }
}
