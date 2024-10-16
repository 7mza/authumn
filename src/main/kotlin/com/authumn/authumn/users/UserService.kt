package com.authumn.authumn.users

import com.authumn.authumn.commons.CustomConstraintViolationException
import com.authumn.authumn.commons.CustomResourceNotFoundException
import com.authumn.authumn.commons.EMAIL_MUST_BE_UNIQUE
import com.authumn.authumn.commons.ICrud
import com.authumn.authumn.roles.IRoleService
import com.authumn.authumn.roles.Role
import jakarta.transaction.Transactional
import jakarta.validation.ConstraintViolationException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Transactional
interface IUserService : ICrud<UserPostDto, UserPutDto, User> {
    fun findByEmail(email: String): User
}

@Service
class UserService
    @Autowired
    constructor(
        private val userRepo: IUserRepo,
        private val roleService: IRoleService,
        private val passwordEncoder: PasswordEncoder,
    ) : IUserService {
        override fun save(t: UserPostDto): User {
            try {
                val defaultRoles = roleService.findAllByIsDefaultTrue()
                val roles = roleService.findManyByIds(t.roles)
                return userRepo.save(t.fromDto(passwordEncoder, (defaultRoles + roles).distinct()))
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
                                if (it.contains("user_email_unique", true)) {
                                    throw CustomConstraintViolationException(EMAIL_MUST_BE_UNIQUE)
                                }
                            }
                        }
                        throw ex
                    }
                }
            }
        }

        override fun saveMany(t: Collection<UserPostDto>): Collection<User> {
            try {
                return userRepo.saveAll(
                    t.map {
                        val defaultRoles = roleService.findAllByIsDefaultTrue()
                        val roles = it.roles.let { it2 -> roleService.findManyByIds(it2) }
                        it.fromDto(passwordEncoder, (defaultRoles + roles).distinct())
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
                                if (it.contains("user_email_unique", true)) {
                                    throw CustomConstraintViolationException(EMAIL_MUST_BE_UNIQUE)
                                }
                            }
                        }
                        throw ex
                    }
                }
            }
        }

        override fun findById(id: String): User =
            userRepo.findById(id).orElseThrow {
                CustomResourceNotFoundException("User with id: $id not found")
            }

        override fun findManyByIds(ids: Collection<String>?): Collection<User> = ids?.let { userRepo.findAllById(it) } ?: emptySet()

        override fun findAll(): Collection<User> = userRepo.findAll()

        override fun update(
            id: String,
            u: UserPutDto,
        ): User {
            try {
                val result = userRepo.findById(id).orElseThrow { CustomResourceNotFoundException("User with id: $id not found") }
                result.email = u.email
                result.password =
                    if (u.password.isBlank()) {
                        throw CustomConstraintViolationException("password must not be blank or empty")
                    } else {
                        passwordEncoder.encode(u.password)
                    }
                result.roles = roleService.findManyByIds(u.roles)
                return userRepo.save(result)
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
                                if (it.contains("user_email_unique", true)) {
                                    throw CustomConstraintViolationException(EMAIL_MUST_BE_UNIQUE)
                                }
                            }
                        }
                        throw ex
                    }
                }
            }
        }

        override fun deleteById(id: String) {
            if (existsById(id).not()) throw CustomResourceNotFoundException("User with id: $id not found")
            return userRepo.deleteById(id)
        }

        override fun findByEmail(email: String): User =
            userRepo.findByEmail(email) ?: throw CustomResourceNotFoundException("User with email: $email not found")

        override fun deleteAll() = userRepo.deleteAll()

        override fun count(): Long = userRepo.count()

        override fun existsById(id: String): Boolean = userRepo.existsById(id)
    }

fun UserPostDto.fromDto(
    passwordEncoder: PasswordEncoder,
    roles: Collection<Role>,
) = User(
    id = "",
    email = this.email,
    password =
        if (this.password.isBlank()) {
            throw CustomConstraintViolationException("password must not be blank or empty")
        } else {
            passwordEncoder.encode(this.password)
        },
    roles = roles,
)
