package com.authumn.authumn.roles

import com.authumn.authumn.privileges.PrivilegeGetDto
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class RoleGetDto(
    val id: String,
    val label: String,
    val isDefault: Boolean,
    val privileges: Collection<PrivilegeGetDto>,
    val createdAt: String,
    val updateAt: String,
)

data class RolePostDto(
    @field:NotBlank(message = "label must not be blank")
    @field:NotEmpty(message = "label must not be empty")
    @Schema(
        description = "role label, must not be blank or empty, must be unique",
        defaultValue = "user",
    )
    val label: String,
    @Schema(
        description = "default roles are automatically picked when creating a new user",
        defaultValue = "false",
    )
    val isDefault: Boolean,
    @Schema(
        description = "list of privilege ids",
        defaultValue = "[]",
    )
    val privileges: Collection<String>?,
)

data class RolePutDto(
    @field:NotBlank(message = "label must not be blank")
    @field:NotEmpty(message = "label must not be empty")
    @Schema(
        description = "role label, must not be blank or empty, must be unique",
        defaultValue = "user",
    )
    val label: String,
    @Schema(
        description = "default roles are automatically picked when creating a new user",
        defaultValue = "false",
    )
    val isDefault: Boolean,
    @Schema(
        description = "list of privilege ids",
        defaultValue = "[]",
    )
    val privileges: Collection<String>,
)
