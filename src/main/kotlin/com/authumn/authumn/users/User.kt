package com.authumn.authumn.users

import com.authumn.authumn.commons.Commons
import com.authumn.authumn.roles.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity
@Table(
    name = "users",
    uniqueConstraints = [UniqueConstraint(columnNames = ["email"], name = "user_email_unique")],
)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,
    @Column(nullable = false)
    @field:NotBlank(message = "email must not be blank")
    @field:NotEmpty(message = "email must not be empty")
    @field:Email(message = "email not valid")
    var email: String,
    @Column(columnDefinition = "TEXT", nullable = false)
    @field:NotBlank(message = "password must not be blank")
    @field:NotEmpty(message = "password must not be empty")
    var password: String,
    // @field:NotEmpty(message = "roles must not be empty")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = [JoinColumn(name = "userId", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "roleId", referencedColumnName = "id")],
    )
    var roles: Collection<Role>,
    @Column(nullable = false)
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val createdAt: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    val updateAt: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
) {
    fun toDto() =
        UserGetDto(
            id = this.id,
            email = this.email,
            roles = this.roles.map { it.toDto() },
            createdAt = Commons.instantToString(this.createdAt),
            updateAt = Commons.instantToString(this.createdAt),
        )
}

interface IUserRepo : JpaRepository<User, String> {
    fun findByEmail(email: String): User?
}
