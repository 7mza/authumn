package com.authumn.authumn.roles

import com.authumn.authumn.commons.CustomConstraintViolationException
import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.authumn.authumn.commons.ICrud
import com.authumn.authumn.commons.LABEL_MUST_BE_UNIQUE
import com.authumn.authumn.privileges.IPrivilegeService
import com.authumn.authumn.privileges.Privilege
import jakarta.transaction.Transactional
import jakarta.validation.ConstraintViolationException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Transactional
interface IRoleService : ICrud<RolePostDto, RolePutDto, Role> {
    fun findAllByIsDefaultTrue(): Collection<Role>
}

@Service
class RoleService
    @Autowired
    constructor(
        private val roleRepo: IRoleRepo,
        private val privilegeService: IPrivilegeService,
    ) : IRoleService {
        override fun save(t: RolePostDto): Role {
            try {
                val defaultPrivileges = privilegeService.findAllByIsDefaultTrue()
                val privileges = privilegeService.findManyByIds(t.privileges)
                return roleRepo.save(t.fromDto((defaultPrivileges + privileges).distinct()))
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
                                if (it.contains("role_label_unique", true)) {
                                    throw CustomConstraintViolationException(LABEL_MUST_BE_UNIQUE)
                                }
                            }
                        }
                        throw ex
                    }
                }
            }
        }

        override fun saveMany(t: Collection<RolePostDto>): Collection<Role> {
            try {
                return roleRepo.saveAll(
                    t.map {
                        val defaultPrivileges = privilegeService.findAllByIsDefaultTrue()
                        val privileges = it.privileges.let { it2 -> privilegeService.findManyByIds(it2) }
                        it.fromDto((defaultPrivileges + privileges).distinct())
                    },
                )
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
                                if (it.contains("role_label_unique", true)) {
                                    throw CustomConstraintViolationException(LABEL_MUST_BE_UNIQUE)
                                }
                            }
                        }
                        throw ex
                    }
                }
            }
        }

        override fun findById(id: String): Role =
            roleRepo.findById(id).orElseThrow {
                CustomResourceNotFoundException("Role with id: $id not found")
            }

        override fun findManyByIds(ids: Collection<String>?): Collection<Role> = ids?.let { roleRepo.findAllById(it) } ?: emptySet()

        override fun findAll(): Collection<Role> = roleRepo.findAll()

        override fun update(
            id: String,
            u: RolePutDto,
        ): Role {
            try {
                val result =
                    roleRepo.findById(id).orElseThrow { CustomResourceNotFoundException("Role with id: $id not found") }
                result.label = u.label
                result.privileges = privilegeService.findManyByIds(u.privileges)
                if (result.privileges.isEmpty()) throw CustomConstraintViolationException("privileges must not be empty")
                return roleRepo.save(result)
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
                                if (it.contains("role_label_unique", true)) {
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
            if (existsById(id).not()) throw CustomResourceNotFoundException("Role with id: $id not found")
            return roleRepo.deleteById(id)
        }

        override fun deleteAll() = roleRepo.deleteAll()

        override fun findAllByIsDefaultTrue(): Collection<Role> = roleRepo.findAllByIsDefaultTrue()

        override fun count(): Long = roleRepo.count()

        override fun existsById(id: String): Boolean = roleRepo.existsById(id)
    }

fun RolePostDto.fromDto(privileges: Collection<Privilege>) =
    Role(
        id = "",
        label = this.label.lowercase(),
        isDefault = this.isDefault ?: false,
        privileges = privileges,
    )
