package com.authumn.authumn.web

import com.authumn.authumn.commons.AuthenticationFacade
import com.authumn.authumn.users.IUserService
import com.authumn.authumn.users.UserPostDto
import com.authumn.authumn.users.UserPutDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping(value = ["/user"], produces = [MediaType.TEXT_HTML_VALUE])
interface IUserWeb {
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping
    fun save(
        @ModelAttribute
        @Valid
        t: UserPostDto,
        model: Model,
    ): String

    @PreAuthorize("hasAuthority('admin') or (hasAuthority('user') and #id == principal?.id)")
    @GetMapping("/{id}")
    fun findById(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
        model: Model,
    ): String

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping
    fun findAll(model: Model): String

    @PreAuthorize("hasAuthority('admin') or (hasAuthority('user') and #id == principal?.id)")
    @PutMapping("/{id}")
    fun update(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
        @ModelAttribute
        @Valid
        x: UserPutDto,
        model: Model,
    ): String

    @PreAuthorize("hasAuthority('admin') or (hasAuthority('user') and #id == principal?.id)")
    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
        model: Model,
    ): String
}

@Controller
class UserWebCtrl
    @Autowired
    constructor(
        private val authenticationFacade: AuthenticationFacade,
        private val service: IUserService,
    ) : IUserWeb {
        override fun save(
            t: UserPostDto,
            model: Model,
        ): String = throw NotImplementedError()

        override fun findById(
            id: String,
            model: Model,
        ): String = throw NotImplementedError()

        override fun findAll(model: Model): String = throw NotImplementedError()

        override fun update(
            id: String,
            x: UserPutDto,
            model: Model,
        ): String = throw NotImplementedError()

        override fun deleteById(
            id: String,
            model: Model,
        ): String = throw NotImplementedError()
    }
