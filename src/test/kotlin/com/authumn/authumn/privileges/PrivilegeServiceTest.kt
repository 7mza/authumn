package com.authumn.authumn.privileges

import com.authumn.authumn.commons.CustomConstraintViolationException
import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.authumn.authumn.roles.IRoleRepo
import com.authumn.authumn.roles.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PrivilegeServiceTest {
    @Autowired
    private lateinit var privilegeRepo: IPrivilegeRepo

    @Autowired
    private lateinit var roleRepo: IRoleRepo

    @Autowired
    private lateinit var service: IPrivilegeService

    @AfterEach
    fun afterEach() {
        roleRepo.deleteAll()
        privilegeRepo.deleteAll()
    }

    @Test
    fun save() {
        val dto = PrivilegePostDto("priv1", false)
        val saved = service.save(dto)
        assertThat(saved.id).isInstanceOf(String::class.java)
        assertThat(saved.label).isEqualTo(dto.label)
        assertThat(saved.createdAt).isBefore(Instant.now())
        assertThat(saved.updateAt).isBefore(Instant.now())
    }

    @Test
    fun `save default is added automatically to existing roles`() {
        roleRepo.save(
            Role(
                id = "",
                label = "role1",
                privileges = listOf(service.save(PrivilegePostDto(label = "priv1", isDefault = false))),
            ),
        )
        roleRepo.save(
            Role(
                id = "",
                label = "role2",
                privileges = listOf(service.save(PrivilegePostDto(label = "priv2", isDefault = false))),
            ),
        )
        service.save(PrivilegePostDto(label = "priv3", isDefault = true))
        val roles = roleRepo.findAll()
        assertThat(roles.size).isEqualTo(2)
        assertThat(roles.first().privileges.size).isEqualTo(2)
        assertThat(roles.last().privileges.size).isEqualTo(2)
    }

    @Test
    fun `save with an existing label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto = PrivilegePostDto("priv1", false)
                service.save(dto)
                service.save(dto)
            }
        assertThat(ex.message).isEqualTo("label must be unique")
        assertThat(privilegeRepo.count()).isEqualTo(1)
    }

    @Test
    fun `save with an empty label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(PrivilegePostDto("", false))
            }
        assertThat(ex.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(privilegeRepo.count()).isZero
    }

    @Test
    fun `save with a blank label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(PrivilegePostDto(" ", false))
            }
        assertThat(ex.message).isEqualTo("label must not be blank")
        assertThat(privilegeRepo.count()).isZero
    }

    @Test
    fun saveMany() {
        val dto1 = PrivilegePostDto("priv1", false)
        val dto2 = PrivilegePostDto("priv2", false)
        val saved = service.saveMany(listOf(dto1, dto2)).sortedBy { it.createdAt }
        assertThat(saved.size).isEqualTo(2)
        assertThat(saved.first().id).isInstanceOf(String::class.java)
        assertThat(saved.first().label).isEqualTo(dto1.label)
        assertThat(saved.first().createdAt).isBefore(Instant.now())
        assertThat(saved.first().updateAt).isBefore(Instant.now())
        assertThat(saved.last().id).isInstanceOf(String::class.java)
        assertThat(saved.last().label).isEqualTo(dto2.label)
        assertThat(saved.last().createdAt).isBefore(Instant.now())
        assertThat(saved.last().updateAt).isBefore(Instant.now())
    }

    @Test
    fun `saveMany default are added automatically to existing roles`() {
        roleRepo.save(
            Role(
                id = "",
                label = "role1",
                privileges = listOf(service.save(PrivilegePostDto(label = "priv1", isDefault = false))),
            ),
        )
        roleRepo.save(
            Role(
                id = "",
                label = "role2",
                privileges = listOf(service.save(PrivilegePostDto(label = "priv2", isDefault = false))),
            ),
        )
        service.saveMany(
            listOf(
                PrivilegePostDto(label = "priv3", isDefault = true),
                PrivilegePostDto(label = "priv4", isDefault = true),
                PrivilegePostDto(label = "priv5", isDefault = false),
            ),
        )
        val roles = roleRepo.findAll()
        assertThat(roles.size).isEqualTo(2)
        assertThat(roles.first().privileges.size).isEqualTo(4)
        assertThat(roles.first().privileges.find { it.label == "priv5" }).isNull()
        assertThat(roles.last().privileges.size).isEqualTo(4)
        assertThat(roles.last().privileges.find { it.label == "priv5" }).isNull()
    }

    @Test
    fun `saveMany with an existing label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = PrivilegePostDto("priv1", false)
                val dto2 = PrivilegePostDto("priv1", false)
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("label must be unique")
        assertThat(privilegeRepo.count()).isEqualTo(0)
    }

    @Test
    fun `saveMany with an empty label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = PrivilegePostDto("priv1", false)
                val dto2 = PrivilegePostDto("", false)
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(privilegeRepo.count()).isZero
    }

    @Test
    fun `saveMany with a blank label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = PrivilegePostDto("priv1", false)
                val dto2 = PrivilegePostDto(" ", false)
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("label must not be blank")
        assertThat(privilegeRepo.count()).isZero
    }

    @Test
    fun findById() {
        val dto = PrivilegePostDto("priv1", false)
        val saved = service.save(dto)
        val found = service.findById(saved.id)
        assertThat(found).isEqualTo(saved)
    }

    @Test
    fun `find by a non existing id`() {
        val ex =
            assertThrows<CustomResourceNotFoundException> {
                service.findById("1")
            }
        assertThat(ex.message).isEqualTo("Privilege with id: 1 not found")
    }

    @Test
    fun findManyByIds() {
        val dto1 = PrivilegePostDto("priv1", false)
        val dto2 = PrivilegePostDto("priv2", false)
        val dto3 = PrivilegePostDto("priv3", false)
        val saved = service.saveMany(listOf(dto1, dto2, dto3)).sortedBy { it.createdAt }
        val found = service.findManyByIds(listOf(saved.first().id, saved.last().id))
        assertThat(found.size).isEqualTo(2)
        assertThat(found).hasSameElementsAs(
            saved.filterNot { it.label == dto2.label },
        )
    }

    @Test
    fun `findMany by a non existing id`() {
        val dto = PrivilegePostDto("priv1", false)
        val saved = service.save(dto)
        val found = service.findManyByIds(listOf(saved.id, "1"))
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first()).isEqualTo(saved)
    }

    @Test
    fun findAll() {
        val dto1 = PrivilegePostDto("priv1", false)
        val dto2 = PrivilegePostDto("priv2", false)
        val dto3 = PrivilegePostDto("priv3", false)
        val saved = service.saveMany(listOf(dto1, dto2, dto3))
        val found = service.findAll()
        assertThat(found.size).isEqualTo(3)
        assertThat(found).hasSameElementsAs(saved)
    }

    @Test
    fun update() {
        val dto = PrivilegePostDto("priv1", false)
        val saved = service.save(dto)
        val updated = service.update(saved.id, PrivilegePutDto("_priv1", false))
        assertThat(updated.id).isEqualTo(saved.id)
        assertThat(updated.label).isEqualTo("_priv1")
        assertThat(updated.updateAt).isAfter(saved.updateAt)
        assertThat(saved.label).isEqualTo("priv1")
    }

    @Test
    fun `update with no change`() {
        val dto = PrivilegePostDto("priv1", false)
        val saved = service.save(dto)
        val updated = service.update(saved.id, PrivilegePutDto("priv1", false))
        assertThat(updated).isEqualTo(saved)
    }

    @Test
    fun `update default is added automatically to existing roles`() {
        roleRepo.save(
            Role(
                id = "",
                label = "role1",
                privileges = listOf(service.save(PrivilegePostDto(label = "priv1", isDefault = false))),
            ),
        )
        val dto = PrivilegePostDto("priv2", false)
        val saved = service.save(dto)
        val updated = service.update(saved.id, PrivilegePutDto("priv2", true))
        assertThat(updated.id).isEqualTo(saved.id)
        assertThat(updated.label).isEqualTo(saved.label)
        assertThat(updated.isDefault).isTrue()
        assertThat(updated.updateAt).isAfter(saved.updateAt)
        val roles = roleRepo.findAll()
        assertThat(roles.size).isOne()
        assertThat(roles.first().privileges.size).isEqualTo(2)
    }

    @Test
    fun `update with an existing label`() {
        val dto1 = PrivilegePostDto("priv1", false)
        val dto2 = PrivilegePostDto("priv2", false)
        val saved = service.saveMany(listOf(dto1, dto2)).sortedBy { it.createdAt }
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(saved.first().id, PrivilegePutDto("priv2", false))
            }
        assertThat(ex.message).isEqualTo("label must be unique")
        assertThat(saved.first().label).isEqualTo(dto1.label)
        assertThat(saved.last().label).isEqualTo(dto2.label)
    }

    @Test
    fun `update with an empty label`() {
        val dto = PrivilegePostDto("priv1", false)
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(saved.id, PrivilegePutDto("", false))
            }
        assertThat(ex.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(saved.label).isEqualTo(dto.label)
    }

    @Test
    fun `update with a blank label`() {
        val dto = PrivilegePostDto("priv1", false)
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(saved.id, PrivilegePutDto(" ", false))
            }
        assertThat(ex.message).isEqualTo("label must not be blank")
        assertThat(saved.label).isEqualTo(dto.label)
    }

    @Test
    fun deleteById() {
        val dto = PrivilegePostDto("priv1", false)
        val saved = service.save(dto)
        service.deleteById(saved.id)
        assertThat(privilegeRepo.count()).isEqualTo(0)
    }

    @Test
    fun `deleteById with constraint`() {
        val dto1 = PrivilegePostDto("priv1", false)
        val dto2 = PrivilegePostDto("priv2", false)
        val saved1 = service.save(dto1)
        val saved2 = service.save(dto2)
        roleRepo.save(Role(id = "", label = "role1", privileges = mutableSetOf(saved1, saved2)))
        service.deleteById(saved1.id)
        assertThat(privilegeRepo.count()).isEqualTo(1)
        val role = roleRepo.findAll().first()
        assertThat(role.privileges.size).isOne()
        assertThat(role.privileges.first()).isEqualTo(saved2)
    }

    @Test
    fun `delete by a non existing id`() {
        val ex =
            assertThrows<CustomResourceNotFoundException> {
                service.deleteById("1")
            }
        assertThat(ex.message).isEqualTo("Privilege with id: 1 not found")
    }

    @Test
    fun deleteAll() {
        val dto1 = PrivilegePostDto("priv1", false)
        val dto2 = PrivilegePostDto("priv2", false)
        val dto3 = PrivilegePostDto("priv3", false)
        service.saveMany(listOf(dto1, dto2, dto3))
        service.deleteAll()
        assertThat(privilegeRepo.count()).isEqualTo(0)
    }

    @Test
    fun findAllByIsDefaultTrue() {
        val dto1 = PrivilegePostDto("priv1", true)
        val dto2 = PrivilegePostDto("priv2", false)
        val dto3 = PrivilegePostDto("priv3", true)
        service.saveMany(listOf(dto1, dto2, dto3))
        val found = service.findAllByIsDefaultTrue().sortedBy { it.createdAt }
        assertThat(found.size).isEqualTo(2)
        assertThat(found.first().label).isEqualTo(dto1.label)
        assertThat(found.last().label).isEqualTo(dto3.label)
    }

    @Test
    fun count() {
        val dto1 = PrivilegePostDto("priv1", true)
        val dto2 = PrivilegePostDto("priv2", false)
        val dto3 = PrivilegePostDto("priv3", true)
        service.saveMany(listOf(dto1, dto2, dto3))
        assertThat(service.count()).isEqualTo(3)
    }

    @Test
    fun addToRoles() {
        roleRepo.save(
            Role(
                id = "",
                label = "role1",
                privileges = listOf(service.save(PrivilegePostDto(label = "priv1", isDefault = false))),
            ),
        )
        val priv = privilegeRepo.save(Privilege(id = "", label = "priv2", isDefault = true))
        service.addToRoles(priv)
        val roles = roleRepo.findAll()
        assertThat(roles.size).isOne()
        assertThat(roles.first().privileges.size).isEqualTo(2)
    }

    @Test
    fun `addToRoles non default`() {
        roleRepo.save(
            Role(
                id = "",
                label = "role1",
                privileges = listOf(service.save(PrivilegePostDto(label = "priv1", isDefault = false))),
            ),
        )
        val priv = privilegeRepo.save(Privilege(id = "", label = "priv2", isDefault = false))
        service.addToRoles(priv)
        val roles = roleRepo.findAll()
        assertThat(roles.size).isOne()
        assertThat(roles.first().privileges.size).isEqualTo(1)
        assertThat(
            roles
                .first()
                .privileges
                .first()
                .label,
        ).isNotEqualTo(priv.label)
    }

    @Test
    fun `addToRoles non existing`() {
        roleRepo.save(
            Role(
                id = "",
                label = "role1",
                privileges = listOf(service.save(PrivilegePostDto(label = "priv1", isDefault = false))),
            ),
        )
        val priv = Privilege(id = "x", label = "priv2", isDefault = false)
        service.addToRoles(priv)
        val roles = roleRepo.findAll()
        assertThat(roles.size).isOne()
        assertThat(roles.first().privileges.size).isEqualTo(1)
        assertThat(
            roles
                .first()
                .privileges
                .first()
                .label,
        ).isNotEqualTo(priv.label)
    }

    @Test
    fun removeFromRoles() {
        val saved = privilegeRepo.save(Privilege(id = "", label = "priv1", isDefault = false))
        roleRepo.save(Role(id = "", label = "role1", privileges = mutableSetOf(saved)))
        assertThat(roleRepo.count()).isOne()
        service.removeFromRoles(saved.id)
        assertThat(privilegeRepo.count()).isOne()
        val role = roleRepo.findAll().first()
        assertThat(role.privileges.size).isZero()
    }

    @Test
    fun `removeFromRoles non existing`() {
        val saved = privilegeRepo.save(Privilege(id = "", label = "priv1", isDefault = false))
        roleRepo.save(Role(id = "", label = "role1", privileges = mutableSetOf(saved)))
        assertThat(roleRepo.count()).isOne()
        service.removeFromRoles("x")
        assertThat(privilegeRepo.count()).isOne()
        val role = roleRepo.findAll().first()
        assertThat(role.privileges.size).isOne()
    }
}
