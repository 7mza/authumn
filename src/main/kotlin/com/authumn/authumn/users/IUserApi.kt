package com.authumn.authumn.users

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.OAuthScope
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus

@SecurityScheme(
    name = "security_auth",
    type = SecuritySchemeType.OAUTH2,
    flows =
        OAuthFlows(
            clientCredentials =
                OAuthFlow(
                    tokenUrl = "http://127.0.0.1:9000/oauth2/token", // FIXME: should point to a gateway
                    scopes = [
                        OAuthScope(
                            name = "admin",
                            description = "admin scope",
                        ),
                        OAuthScope(
                            name = "user",
                            description = "user scope",
                        ),
                    ],
                ),
        ),
)
@Tag(name = "users", description = "auth-server user operations")
@RequestMapping(value = ["/api/user"], produces = [MediaType.APPLICATION_JSON_VALUE])
interface IUserApi {
    @PostMapping
    @Operation(
        summary = "create a user",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = UserGetDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                  "id": "1",
                                  "email": "aa@aa.com",
                                  "roles": [
                                    {
                                      "id": "2",
                                      "label": "role",
                                      "isDefault": true,
                                      "privileges": [{ "id": "3", "label": "priv", "isDefault": false }]
                                    }
                                  ]
                                }                                    
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun save(
        @RequestBody
        @Valid
        t: UserPostDto,
    ): UserGetDto

    @SecurityRequirement(
        name = "security_auth",
        scopes = ["admin", "user"],
    )
    @GetMapping("/{id}")
    @Operation(
        summary = "find a user by id",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = UserGetDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                  "id": "1",
                                  "email": "aa@aa.com",
                                  "roles": [
                                    {
                                      "id": "2",
                                      "label": "role",
                                      "isDefault": true,
                                      "privileges": [{ "id": "3", "label": "priv", "isDefault": false }]
                                    }
                                  ]
                                }                                    
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun findById(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
    ): UserGetDto?

    @SecurityRequirement(
        name = "security_auth",
        scopes = ["admin"],
    )
    @GetMapping
    @Operation(
        summary = "find all users",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array = ArraySchema(items = Schema(implementation = UserGetDto::class)),
                        schema = Schema(implementation = UserGetDto::class),
                        examples = [
                            ExampleObject(
                                """
                                [
                                  {
                                    "id": "1",
                                    "email": "aa@aa.com",
                                    "roles": [
                                      {
                                        "id": "2",
                                        "label": "role",
                                        "isDefault": true,
                                        "privileges": [{ "id": "3", "label": "priv", "isDefault": false }]
                                      }
                                    ]
                                  }
                                ]                                    
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun findAll(): Collection<UserGetDto>

    @SecurityRequirement(
        name = "security_auth",
        scopes = ["admin", "user"],
    )
    @PutMapping("/{id}")
    @Operation(
        summary = "update a user",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = UserGetDto::class),
                        examples = [
                            ExampleObject(
                                """
                                {
                                  "id": "1",
                                  "email": "aa@aa.com",
                                  "roles": [
                                    {
                                      "id": "2",
                                      "label": "role",
                                      "isDefault": true,
                                      "privileges": [{ "id": "3", "label": "priv", "isDefault": false }]
                                    }
                                  ]
                                }                                    
                                """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun update(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
        @RequestBody
        @Valid
        x: UserPutDto,
    ): UserGetDto

    @SecurityRequirement(
        name = "security_auth",
        scopes = ["admin", "user"],
    )
    @DeleteMapping("/{id}")
    @Operation(
        summary = "delete a user by id",
        description = "TODO",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "No Content",
            ),
        ],
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun deleteById(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
    )
}
