package com.authumn.authumn.privileges

import com.authumn.authumn.commons.CustomConstraintViolationException
import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.authumn.authumn.commons.ICrud
import com.authumn.authumn.commons.LABEL_MUST_BE_UNIQUE
import jakarta.transaction.Transactional
import jakarta.validation.ConstraintViolationException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Transactional
interface IPrivilegeService : ICrud<PrivilegePostDto, PrivilegePutDto, Privilege> {
    fun findAllByIsDefaultTrue(): Collection<Privilege>

    fun addToRoles(privilege: Privilege): Int

    fun removeFromRoles(id: String): Int
}

@Service
class PrivilegeService
    @Autowired
    constructor(
        private val privilegeRepo: IPrivilegeRepo,
        private val jdbcTemplate: JdbcTemplate,
    ) : IPrivilegeService {
        override fun save(t: PrivilegePostDto): Privilege {
            try {
                val privilege = privilegeRepo.save(t.fromDto())
                this.addToRoles(privilege)
                return privilege
            } catch (ex: Throwable) {
                when (val root = ExceptionUtils.getRootCause(ex)) {
                    is ConstraintViolationException -> {
                        throw CustomConstraintViolationException(
                            root.constraintViolations
                                .map { it.message }
                                .sortedBy { it }
                                .joinToString(". "),
                        )
                    }
                    else -> {
                        if (ExceptionUtils.indexOfType(ex, org.hibernate.exception.ConstraintViolationException::class.java) != -1) {
                            (ex.cause as org.hibernate.exception.ConstraintViolationException).constraintName?.let {
                                if (it.contains("privilege_label_unique", true)) {
                                    throw CustomConstraintViolationException(LABEL_MUST_BE_UNIQUE)
                                }
                            }
                        }
                        throw ex
                    }
                }
            }
        }

        override fun saveMany(t: Collection<PrivilegePostDto>): Collection<Privilege> {
            try {
                val privileges = privilegeRepo.saveAll(t.map { it.fromDto() })
                privileges.forEach {
                    this.addToRoles(it)
                }
                return privileges
            } catch (ex: Throwable) {
                when (val root = ExceptionUtils.getRootCause(ex)) {
                    is ConstraintViolationException -> {
                        throw CustomConstraintViolationException(
                            root.constraintViolations
                                .map { it.message }
                                .sortedBy { it }
                                .joinToString(". "),
                        )
                    }
                    else -> {
                        if (ExceptionUtils.indexOfType(ex, org.hibernate.exception.ConstraintViolationException::class.java) != -1) {
                            (ex.cause as org.hibernate.exception.ConstraintViolationException).constraintName?.let {
                                if (it.contains("privilege_label_unique", true)) {
                                    throw CustomConstraintViolationException(LABEL_MUST_BE_UNIQUE)
                                }
                            }
                        }
                        throw ex
                    }
                }
            }
        }

        override fun findById(id: String): Privilege =
            privilegeRepo.findById(id).orElseThrow {
                CustomResourceNotFoundException("Privilege with id: $id not found")
            }

        override fun findManyByIds(ids: Collection<String>?): Collection<Privilege> =
            ids?.let { privilegeRepo.findAllById(it) } ?: emptySet()

        override fun findAll(): Collection<Privilege> = privilegeRepo.findAll()

        override fun update(
            id: String,
            u: PrivilegePutDto,
        ): Privilege {
            try {
                val result =
                    privilegeRepo.findById(id).getOrNull() ?: throw CustomResourceNotFoundException("Privilege with id: $id not found")
                result.label = u.label
                return privilegeRepo.save(result)
            } catch (ex: Throwable) {
                when (val root = ExceptionUtils.getRootCause(ex)) {
                    is ConstraintViolationException -> {
                        throw CustomConstraintViolationException(
                            root.constraintViolations
                                .map { it.message }
                                .sortedBy { it }
                                .joinToString(". "),
                        )
                    }
                    else -> {
                        if (ExceptionUtils.indexOfType(ex, org.hibernate.exception.ConstraintViolationException::class.java) != -1) {
                            (ex.cause as org.hibernate.exception.ConstraintViolationException).constraintName?.let {
                                if (it.contains("privilege_label_unique", true)) {
                                    throw CustomConstraintViolationException(LABEL_MUST_BE_UNIQUE)
                                }
                            }
                        }
                        throw ex
                    }
                }
            }
        }

        override fun deleteById(id: String) {
            if (existsById(id).not()) throw CustomResourceNotFoundException("Privilege with id: $id not found")
            this.removeFromRoles(id)
            return privilegeRepo.deleteById(id)
        }

        override fun deleteAll() = privilegeRepo.deleteAll()

        override fun findAllByIsDefaultTrue(): Collection<Privilege> = privilegeRepo.findAllByIsDefaultTrue()

        override fun count(): Long = privilegeRepo.count()

        override fun existsById(id: String): Boolean = privilegeRepo.existsById(id)

        override fun addToRoles(privilege: Privilege): Int =
            if (privilege.isDefault.not() || this.existsById(privilege.id).not()) {
                0
            } else {
                jdbcTemplate.update(
                    """
                    INSERT INTO roles_privileges (privilege_id, role_id)
                    SELECT ?, role_id
                    FROM roles_privileges
                    WHERE NOT EXISTS (
                        SELECT 1 
                        FROM roles_privileges rp
                        WHERE rp.privilege_id = ? 
                        AND rp.role_id = roles_privileges.role_id
                    );
                    """.trimIndent(),
                    privilege.id,
                    privilege.id,
                )
            }

        override fun removeFromRoles(id: String): Int =
            if (this.existsById(id)) jdbcTemplate.update("DELETE FROM roles_privileges WHERE privilege_id = ?", id) else 0
    }

fun PrivilegePostDto.fromDto() =
    Privilege(
        id = "",
        label = this.label.lowercase(),
        isDefault = this.isDefault ?: false,
    )
