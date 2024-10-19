package com.authumn.authumn.users

import com.authumn.authumn.commons.CustomConstraintViolationException
import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.authumn.authumn.privileges.IPrivilegeRepo
import com.authumn.authumn.privileges.Privilege
import com.authumn.authumn.roles.IRoleRepo
import com.authumn.authumn.roles.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceTest {
    @Autowired
    private lateinit var userRepo: IUserRepo

    @Autowired
    private lateinit var roleRepo: IRoleRepo

    @Autowired
    private lateinit var privilegeRepo: IPrivilegeRepo

    @Autowired
    private lateinit var service: IUserService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var privilege1: Privilege

    private lateinit var privilege2: Privilege

    private lateinit var privilege3: Privilege

    private lateinit var role1: Role

    private lateinit var role2: Role

    private lateinit var role3: Role

    @BeforeEach
    fun beforeEach() {
        privilege1 = privilegeRepo.save(Privilege(id = "", label = "priv1", isDefault = false))
        privilege2 = privilegeRepo.save(Privilege(id = "", label = "priv2", isDefault = false))
        privilege3 = privilegeRepo.save(Privilege(id = "", label = "priv3", isDefault = false))
        role1 = roleRepo.save(Role(id = "", label = "role1", isDefault = false, privileges = listOf(privilege1, privilege2)))
        role2 = roleRepo.save(Role(id = "", label = "role2", isDefault = false, privileges = listOf(privilege3)))
        role3 = roleRepo.save(Role(id = "", label = "role3", isDefault = false, privileges = listOf(privilege2)))
    }

    @AfterEach
    fun afterEach() {
        userRepo.deleteAll()
        roleRepo.deleteAll()
        privilegeRepo.deleteAll()
    }

    @Test
    fun save() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        assertThat(saved.id).isInstanceOf(String::class.java)
        assertThat(saved.email).isEqualTo(dto.email)
        assertThat(passwordEncoder.matches(dto.password, saved.password)).isTrue
        assertThat(saved.roles.size).isEqualTo(2)
        assertThat(saved.roles.sortedBy { it.createdAt })
            .usingRecursiveComparison()
            .ignoringFields("createdAt", "updateAt")
            .isEqualTo(listOf(role1, role2).sortedBy { it.createdAt })
        assertThat(saved.createdAt).isBefore(Instant.now())
        assertThat(saved.updateAt).isBefore(Instant.now())
    }

    @Disabled
    @Test
    fun `save with empty roles`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf()))
            }
        assertThat(ex.message).isEqualTo("roles must not be empty")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `save with an existing email`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
                service.save(dto)
                service.save(dto)
            }
        assertThat(ex.message).isEqualTo("email must be unique")
        assertThat(userRepo.count()).isEqualTo(1)
    }

    @Test
    fun `save with an empty email`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(UserPostDto(email = "", password = "password1", roles = listOf(role1.id, role2.id)))
            }
        assertThat(ex.message).isEqualTo("email must not be blank. email must not be empty")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `save with a blank email`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(UserPostDto(email = " ", password = "password1", roles = listOf(role1.id, role2.id)))
            }
        assertThat(ex.message).isEqualTo("email must not be blank. email not valid")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `save with a non valid email`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(UserPostDto(email = "aa", password = "password1", roles = listOf(role1.id, role2.id)))
            }
        assertThat(ex.message).isEqualTo("email not valid")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `save with an empty password`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(UserPostDto(email = "user1@mail.com", password = "", roles = listOf(role1.id, role2.id)))
            }
        assertThat(ex.message).isEqualTo("password must not be blank or empty")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `save with a blank password`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(UserPostDto(email = "user1@mail.com", password = " ", roles = listOf(role1.id, role2.id)))
            }
        assertThat(ex.message).isEqualTo("password must not be blank or empty")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `save should pick default roles`() {
        val role4 = roleRepo.save(Role(id = "", label = "role4", isDefault = true, privileges = listOf(privilege2)))
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        assertThat(saved.roles.size).isEqualTo(3)
        assertThat(saved.roles.sortedBy { it.createdAt })
            .usingRecursiveComparison()
            .ignoringFields("createdAt", "updateAt")
            .isEqualTo(listOf(role1, role2, role4).sortedBy { it.createdAt })
    }

    @Test
    fun saveMany() {
        val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val dto2 = UserPostDto(email = "user2@mail.com", password = "password2", roles = listOf(role3.id))
        val saved = service.saveMany(listOf(dto1, dto2)).sortedBy { it.createdAt }
        assertThat(saved.size).isEqualTo(2)
        assertThat(saved.first().id).isInstanceOf(String::class.java)
        assertThat(saved.first().email).isEqualTo(dto1.email)
        assertThat(passwordEncoder.matches(dto1.password, saved.first().password)).isTrue
        assertThat(saved.first().roles.size).isEqualTo(2)
        assertThat(saved.first().roles.sortedBy { it.createdAt })
            .usingRecursiveComparison()
            .ignoringFields("createdAt", "updateAt")
            .isEqualTo(listOf(role1, role2).sortedBy { it.createdAt })
        assertThat(saved.first().createdAt).isBefore(Instant.now())
        assertThat(saved.first().updateAt).isBefore(Instant.now())
        assertThat(saved.last().id).isInstanceOf(String::class.java)
        assertThat(saved.last().email).isEqualTo(dto2.email)
        assertThat(passwordEncoder.matches(dto2.password, saved.last().password)).isTrue
        assertThat(saved.last().roles.size).isEqualTo(1)
        assertThat(saved.last().roles.sortedBy { it.createdAt })
            .usingRecursiveComparison()
            .ignoringFields("createdAt", "updateAt")
            .isEqualTo(listOf(role3).sortedBy { it.createdAt })
        assertThat(saved.last().createdAt).isBefore(Instant.now())
        assertThat(saved.last().updateAt).isBefore(Instant.now())
    }

    @Disabled
    @Test
    fun `saveMany with empty roles`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
                val dto2 = UserPostDto(email = "user2@mail.com", password = "password2", roles = listOf())
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("roles must not be empty")
        assertThat(userRepo.count()).isEqualTo(0)
    }

    @Test
    fun `saveMany with an existing email`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
                val dto2 = UserPostDto(email = "user1@mail.com", password = "password2", roles = listOf(role3.id))
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("email must be unique")
        assertThat(userRepo.count()).isEqualTo(0)
    }

    @Test
    fun `saveMany with an empty email`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
                val dto2 = UserPostDto(email = "", password = "password2", roles = listOf(role3.id))
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("email must not be blank. email must not be empty")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `saveMany with a blank email`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
                val dto2 = UserPostDto(email = " ", password = "password2", roles = listOf(role3.id))
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("email must not be blank. email not valid")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `saveMany with non valid email`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
                val dto2 = UserPostDto(email = "aa", password = "password2", roles = listOf(role3.id))
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("email not valid")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `saveMany with an empty password`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
                val dto2 = UserPostDto(email = "user2@mail.com", password = "", roles = listOf(role3.id))
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("password must not be blank or empty")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun `saveMany with a blank password`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
                val dto2 = UserPostDto(email = "user2@mail.com", password = " ", roles = listOf(role3.id))
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("password must not be blank or empty")
        assertThat(userRepo.count()).isZero
    }

    @Test
    fun findById() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val found = service.findById(saved.id)
        assertThat(found).usingRecursiveComparison().ignoringFields("createdAt", "updateAt").isEqualTo(saved)
    }

    @Test
    fun `find by a non existing id`() {
        val ex =
            assertThrows<CustomResourceNotFoundException> {
                service.findById("1")
            }
        assertThat(ex.message).isEqualTo("User with id: 1 not found")
    }

    @Test
    fun findManyByIds() {
        val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val dto2 = UserPostDto(email = "user2@mail.com", password = "password2", roles = listOf(role3.id))
        val dto3 = UserPostDto(email = "user3@mail.com", password = "password3", roles = listOf(role2.id))
        val saved = service.saveMany(listOf(dto1, dto2, dto3)).sortedBy { it.createdAt }
        val found = service.findManyByIds(listOf(saved.first().id, saved.last().id)).sortedBy { it.createdAt }
        assertThat(found.size).isEqualTo(2)
        assertThat(found.first()).usingRecursiveComparison().ignoringFields("createdAt", "updateAt").isEqualTo(saved.first())
        assertThat(found.last()).usingRecursiveComparison().ignoringFields("createdAt", "updateAt").isEqualTo(saved.last())
    }

    @Test
    fun `findMany by a non existing id`() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val found = service.findManyByIds(listOf(saved.id, "1"))
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first()).usingRecursiveComparison().ignoringFields("createdAt", "updateAt").isEqualTo(saved)
    }

    @Test
    fun findAll() {
        val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val dto2 = UserPostDto(email = "user2@mail.com", password = "password2", roles = listOf(role3.id))
        val dto3 = UserPostDto(email = "user3@mail.com", password = "password3", roles = listOf(role2.id))
        val saved = service.saveMany(listOf(dto1, dto2, dto3))
        val found = service.findAll()
        assertThat(found.size).isEqualTo(3)
        assertThat(found).usingRecursiveComparison().ignoringFields("createdAt", "updateAt").isEqualTo(saved)
    }

    @Test
    fun update() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val updated =
            service.update(
                saved.id,
                UserPutDto(
                    email = "_user1@mail.com",
                    password = "_password1",
                    roles = listOf(role3.id),
                ),
            )
        assertThat(updated.id).isEqualTo(saved.id)
        assertThat(updated.email).isEqualTo("_user1@mail.com")
        assertThat(passwordEncoder.matches("_password1", updated.password)).isTrue
        assertThat(updated.roles).usingRecursiveComparison().ignoringFields("createdAt", "updateAt").isEqualTo(listOf(role3))
        assertThat(updated.updateAt).isAfter(saved.updateAt)
        assertThat(saved.email).isEqualTo("user1@mail.com")
        assertThat(passwordEncoder.matches("password1", saved.password)).isTrue
    }

    @Disabled
    @Test
    fun `update with empty roles`() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(
                    saved.id,
                    UserPutDto(
                        email = "_user1@mail.com",
                        password = "_password1",
                        roles = listOf(),
                    ),
                )
            }
        assertThat(ex.message).isEqualTo("roles must not be empty")
    }

    @Test
    fun `update with an existing email`() {
        val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val dto2 = UserPostDto(email = "user2@mail.com", password = "password2", roles = listOf(role3.id))
        val saved = service.saveMany(listOf(dto1, dto2)).sortedBy { it.createdAt }
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(
                    saved.first().id,
                    UserPutDto(
                        email = "user2@mail.com",
                        password = "_password1",
                        roles = listOf(role1.id, role2.id),
                    ),
                )
            }
        assertThat(ex.message).isEqualTo("email must be unique")
    }

    @Test
    fun `update with an empty email`() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(
                    saved.id,
                    UserPutDto(
                        email = "",
                        password = "password1",
                        roles = listOf(role1.id, role2.id),
                    ),
                )
            }
        assertThat(ex.message).isEqualTo("email must not be blank. email must not be empty")
    }

    @Test
    fun `update with a blank email`() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(
                    saved.id,
                    UserPutDto(
                        email = " ",
                        password = "password1",
                        roles = listOf(role1.id, role2.id),
                    ),
                )
            }
        assertThat(ex.message).isEqualTo("email must not be blank. email not valid")
    }

    @Test
    fun `update with a non valid email`() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(
                    saved.id,
                    UserPutDto(
                        email = "aa",
                        password = "password1",
                        roles = listOf(role1.id, role2.id),
                    ),
                )
            }
        assertThat(ex.message).isEqualTo("email not valid")
    }

    @Test
    fun `update with an empty password`() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(
                    saved.id,
                    UserPutDto(
                        email = "user1@mail.com",
                        password = "",
                        roles = listOf(role1.id, role2.id),
                    ),
                )
            }
        assertThat(ex.message).isEqualTo("password must not be blank or empty")
    }

    @Test
    fun `update with a blank password`() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(
                    saved.id,
                    UserPutDto(
                        email = "user1@mail.com",
                        password = " ",
                        roles = listOf(role1.id, role2.id),
                    ),
                )
            }
        assertThat(ex.message).isEqualTo("password must not be blank or empty")
    }

    @Test
    fun deleteById() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        service.deleteById(saved.id)
        assertThat(userRepo.count()).isEqualTo(0)
        assertThat(roleRepo.count()).isEqualTo(3)
        assertThat(privilegeRepo.count()).isEqualTo(3)
    }

    @Test
    fun `delete by a non existing id`() {
        val ex =
            assertThrows<CustomResourceNotFoundException> {
                service.deleteById("1")
            }
        assertThat(ex.message).isEqualTo("User with id: 1 not found")
    }

    @Test
    fun deleteAll() {
        val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val dto2 = UserPostDto(email = "user2@mail.com", password = "password2", roles = listOf(role3.id))
        val dto3 = UserPostDto(email = "user3@mail.com", password = "password3", roles = listOf(role2.id))
        service.saveMany(listOf(dto1, dto2, dto3))
        service.deleteAll()
        assertThat(userRepo.count()).isEqualTo(0)
        assertThat(roleRepo.count()).isEqualTo(3)
        assertThat(privilegeRepo.count()).isEqualTo(3)
    }

    @Test
    fun findByEmail() {
        val dto = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val saved = service.save(dto)
        val found = service.findByEmail(dto.email)
        assertThat(found).usingRecursiveComparison().ignoringFields("createdAt", "updateAt").isEqualTo(saved)
    }

    @Test
    fun `find by a non existing email`() {
        val ex =
            assertThrows<CustomResourceNotFoundException> {
                service.findByEmail("aa")
            }
        assertThat(ex.message).isEqualTo("User with email: aa not found")
    }

    @Test
    fun count() {
        val dto1 = UserPostDto(email = "user1@mail.com", password = "password1", roles = listOf(role1.id, role2.id))
        val dto2 = UserPostDto(email = "user2@mail.com", password = "password2", roles = listOf(role3.id))
        val dto3 = UserPostDto(email = "user3@mail.com", password = "password3", roles = listOf(role2.id))
        service.saveMany(listOf(dto1, dto2, dto3))
        assertThat(service.count()).isEqualTo(3)
    }
}
