package com.authumn.authumn.users

import com.authumn.authumn.roles.RoleGetDto
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class UserGetDto(
    val id: String,
    val email: String,
    val roles: Collection<RoleGetDto>,
    val createdAt: String,
    val updateAt: String,
)

data class UserPostDto(
    @field:Email(message = "email not valid")
    @field:NotBlank(message = "email must not be blank")
    @field:NotEmpty(message = "email must not be empty")
    @Schema(
        description = "user email, must not be blank or empty, must be unique",
        defaultValue = "user@email.com",
    )
    val email: String,
    @field:NotBlank(message = "password must not be blank")
    @field:NotEmpty(message = "password must not be empty")
    @Schema(
        description = "user password, must not be blank or empty",
        defaultValue = "password",
    )
    val password: String,
    @Schema(
        description = "list of roles ids",
        defaultValue = "[]",
    )
    val roles: Collection<String>?,
)

data class UserPutDto(
    @field:Email(message = "email not valid")
    @field:NotBlank(message = "email must not be blank")
    @field:NotEmpty(message = "email must not be empty")
    @Schema(
        description = "user email, must not be blank or empty, must be unique",
        defaultValue = "user@email.com",
    )
    val email: String,
    @field:NotBlank(message = "password must not be blank")
    @field:NotEmpty(message = "password must not be empty")
    @Schema(
        description = "user password, must not be blank or empty",
        defaultValue = "password",
    )
    val password: String,
    @Schema(
        description = "list of roles ids",
        defaultValue = "[]",
    )
    val roles: Collection<String>,
)
