package com.authumn.authumn.roles

import com.authumn.authumn.commons.CustomConstraintViolationException
import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.authumn.authumn.privileges.IPrivilegeRepo
import com.authumn.authumn.privileges.Privilege
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleServiceTest {
    @Autowired
    private lateinit var roleRepo: IRoleRepo

    @Autowired
    private lateinit var privilegeRepo: IPrivilegeRepo

    @Autowired
    private lateinit var service: IRoleService

    private lateinit var privilege1: Privilege

    private lateinit var privilege2: Privilege

    private lateinit var privilege3: Privilege

    @BeforeEach
    fun beforeEach() {
        privilege1 = privilegeRepo.save(Privilege(id = "", label = "priv1", isDefault = false))
        privilege2 = privilegeRepo.save(Privilege(id = "", label = "priv2", isDefault = false))
        privilege3 = privilegeRepo.save(Privilege(id = "", label = "priv3", isDefault = false))
    }

    @AfterEach
    fun afterEach() {
        roleRepo.deleteAll()
        privilegeRepo.deleteAll()
    }

    @Test
    fun save() {
        val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val saved = service.save(dto)
        assertThat(saved.id).isInstanceOf(String::class.java)
        assertThat(saved.label).isEqualTo(dto.label)
        assertThat(saved.privileges.size).isEqualTo(2)
        assertThat(saved.privileges).hasSameElementsAs(listOf(privilege1, privilege2))
        assertThat(saved.createdAt).isBefore(Instant.now())
        assertThat(saved.updateAt).isBefore(Instant.now())
    }

    @Test
    fun `save with empty privileges`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(RolePostDto("priv1", false, listOf()))
            }
        assertThat(ex.message).isEqualTo("privileges must not be empty")
        assertThat(roleRepo.count()).isZero
    }

    @Test
    fun `save with an existing label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
                service.save(dto)
                service.save(dto)
            }
        assertThat(ex.message).isEqualTo("label must be unique")
        assertThat(roleRepo.count()).isEqualTo(1)
    }

    @Test
    fun `save with an empty label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(RolePostDto("", false, listOf(privilege1.id, privilege2.id)))
            }
        assertThat(ex.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(roleRepo.count()).isZero
    }

    @Test
    fun `save with a blank label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.save(RolePostDto(" ", false, listOf(privilege1.id, privilege2.id)))
            }
        assertThat(ex.message).isEqualTo("label must not be blank")
        assertThat(roleRepo.count()).isZero
    }

    @Test
    fun `save should pick default privileges`() {
        val privilege4 = privilegeRepo.save(Privilege(id = "", label = "priv4", isDefault = true))
        val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val saved = service.save(dto)
        assertThat(saved.privileges.size).isEqualTo(3)
        assertThat(saved.privileges).hasSameElementsAs(listOf(privilege1, privilege2, privilege4))
    }

    @Test
    fun saveMany() {
        val dto1 = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val dto2 = RolePostDto("priv2", false, listOf(privilege3.id))
        val saved = service.saveMany(listOf(dto1, dto2)).sortedBy { it.createdAt }
        assertThat(saved.size).isEqualTo(2)
        assertThat(saved.first().id).isInstanceOf(String::class.java)
        assertThat(saved.first().label).isEqualTo(dto1.label)
        assertThat(saved.first().privileges.size).isEqualTo(2)
        assertThat(saved.first().privileges).hasSameElementsAs(listOf(privilege1, privilege2))
        assertThat(saved.first().createdAt).isBefore(Instant.now())
        assertThat(saved.first().updateAt).isBefore(Instant.now())
        assertThat(saved.last().id).isInstanceOf(String::class.java)
        assertThat(saved.last().label).isEqualTo(dto2.label)
        assertThat(saved.last().privileges.size).isEqualTo(1)
        assertThat(saved.last().privileges).hasSameElementsAs(listOf(privilege3))
        assertThat(saved.last().createdAt).isBefore(Instant.now())
        assertThat(saved.last().updateAt).isBefore(Instant.now())
    }

    @Test
    fun `saveMany with empty privileges`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
                val dto2 = RolePostDto("priv2", false, listOf())
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("privileges must not be empty")
        assertThat(roleRepo.count()).isEqualTo(0)
    }

    @Test
    fun `saveMany with an existing label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
                val dto2 = RolePostDto("priv1", false, listOf(privilege3.id))
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("label must be unique")
        assertThat(roleRepo.count()).isEqualTo(0)
    }

    @Test
    fun `saveMany with an empty label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
                val dto2 = RolePostDto("", false, listOf(privilege3.id))
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(roleRepo.count()).isZero
    }

    @Test
    fun `saveMany with a blank label`() {
        val ex =
            assertThrows<CustomConstraintViolationException> {
                val dto1 = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
                val dto2 = RolePostDto(" ", false, listOf(privilege3.id))
                service.saveMany(listOf(dto1, dto2))
            }
        assertThat(ex.message).isEqualTo("label must not be blank")
        assertThat(roleRepo.count()).isZero
    }

    @Test
    fun findById() {
        val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val saved = service.save(dto)
        val found = service.findById(saved.id)
        assertThat(found).usingRecursiveComparison().ignoringFields("updateAt").isEqualTo(saved)
    }

    @Test
    fun `find by a non existing id`() {
        val ex =
            assertThrows<CustomResourceNotFoundException> {
                service.findById("1")
            }
        assertThat(ex.message).isEqualTo("Role with id: 1 not found")
    }

    @Test
    fun findManyByIds() {
        val dto1 = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val dto2 = RolePostDto("priv2", false, listOf(privilege3.id))
        val dto3 = RolePostDto("priv3", false, listOf(privilege2.id))
        val saved = service.saveMany(listOf(dto1, dto2, dto3)).sortedBy { it.createdAt }
        val found = service.findManyByIds(listOf(saved.first().id, saved.last().id)).sortedBy { it.createdAt }
        assertThat(found.size).isEqualTo(2)
        assertThat(found.first()).usingRecursiveComparison().ignoringFields("updateAt").isEqualTo(saved.first())
        assertThat(found.last()).usingRecursiveComparison().ignoringFields("updateAt").isEqualTo(saved.last())
    }

    @Test
    fun `findMany by a non existing id`() {
        val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val saved = service.save(dto)
        val found = service.findManyByIds(listOf(saved.id, "1"))
        assertThat(found.size).isEqualTo(1)
        assertThat(found.first()).usingRecursiveComparison().ignoringFields("updateAt").isEqualTo(saved)
    }

    @Test
    fun findAll() {
        val dto1 = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val dto2 = RolePostDto("priv2", false, listOf(privilege3.id))
        val dto3 = RolePostDto("priv3", false, listOf(privilege2.id))
        val saved = service.saveMany(listOf(dto1, dto2, dto3))
        val found = service.findAll()
        assertThat(found.size).isEqualTo(3)
        assertThat(found).usingRecursiveComparison().ignoringFields("updateAt").isEqualTo(saved)
    }

    @Test
    fun update() {
        val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val saved = service.save(dto)
        val updated = service.update(saved.id, RolePutDto("_priv1", false, listOf(privilege3.id)))
        assertThat(updated.id).isEqualTo(saved.id)
        assertThat(updated.privileges).hasSameElementsAs(listOf(privilege3))
        assertThat(updated.label).isEqualTo("_priv1")
        assertThat(updated.updateAt).isAfter(saved.updateAt)
        assertThat(saved.label).isEqualTo("priv1")
    }

    @Test
    fun `update with empty privileges`() {
        val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(saved.id, RolePutDto("priv1", false, listOf()))
            }
        assertThat(ex.message).isEqualTo("privileges must not be empty")
    }

    @Test
    fun `update with an existing label`() {
        val dto1 = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val dto2 = RolePostDto("priv2", false, listOf(privilege3.id))
        val saved = service.saveMany(listOf(dto1, dto2)).sortedBy { it.createdAt }
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(saved.first().id, RolePutDto("priv2", false, listOf(privilege3.id)))
            }
        assertThat(ex.message).isEqualTo("label must be unique")
        assertThat(saved.first().label).isEqualTo(dto1.label)
        assertThat(saved.last().label).isEqualTo(dto2.label)
    }

    @Test
    fun `update with an empty label`() {
        val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(saved.id, RolePutDto("", false, listOf(privilege3.id)))
            }
        assertThat(ex.message).isEqualTo("label must not be blank. label must not be empty")
        assertThat(saved.label).isEqualTo(dto.label)
    }

    @Test
    fun `update with a blank label`() {
        val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val saved = service.save(dto)
        val ex =
            assertThrows<CustomConstraintViolationException> {
                service.update(saved.id, RolePutDto(" ", false, listOf(privilege3.id)))
            }
        assertThat(ex.message).isEqualTo("label must not be blank")
        assertThat(saved.label).isEqualTo(dto.label)
    }

    @Test
    fun deleteById() {
        val dto = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val saved = service.save(dto)
        service.deleteById(saved.id)
        assertThat(roleRepo.count()).isEqualTo(0)
        assertThat(privilegeRepo.count()).isEqualTo(3)
    }

    @Test
    fun `delete by a non existing id`() {
        val ex =
            assertThrows<CustomResourceNotFoundException> {
                service.deleteById("1")
            }
        assertThat(ex.message).isEqualTo("Role with id: 1 not found")
    }

    @Test
    fun deleteAll() {
        val dto1 = RolePostDto("priv1", false, listOf(privilege1.id, privilege2.id))
        val dto2 = RolePostDto("priv2", false, listOf(privilege3.id))
        val dto3 = RolePostDto("priv3", false, listOf(privilege2.id))
        service.saveMany(listOf(dto1, dto2, dto3))
        service.deleteAll()
        assertThat(roleRepo.count()).isEqualTo(0)
        assertThat(privilegeRepo.count()).isEqualTo(3)
    }

    @Test
    fun findAllByIsDefaultTrue() {
        val dto1 = RolePostDto("priv1", true, listOf(privilege1.id, privilege2.id))
        val dto2 = RolePostDto("priv2", false, listOf(privilege3.id))
        val dto3 = RolePostDto("priv3", true, listOf(privilege2.id))
        service.saveMany(listOf(dto1, dto2, dto3))
        val found = service.findAllByIsDefaultTrue().sortedBy { it.createdAt }
        assertThat(found.size).isEqualTo(2)
        assertThat(found.first().label).isEqualTo(dto1.label)
        assertThat(found.last().label).isEqualTo(dto3.label)
    }

    @Test
    fun count() {
        val dto1 = RolePostDto("priv1", true, listOf(privilege1.id, privilege2.id))
        val dto2 = RolePostDto("priv2", false, listOf(privilege3.id))
        val dto3 = RolePostDto("priv3", true, listOf(privilege2.id))
        service.saveMany(listOf(dto1, dto2, dto3))
        assertThat(service.count()).isEqualTo(3)
    }
}
