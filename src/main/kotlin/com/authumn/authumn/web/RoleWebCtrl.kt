package com.authumn.authumn.web

import com.authumn.authumn.commons.AuthenticationFacade
import com.authumn.authumn.roles.IRoleService
import com.authumn.authumn.roles.RolePostDto
import com.authumn.authumn.roles.RolePutDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@RequestMapping(value = ["/role"], produces = [MediaType.TEXT_HTML_VALUE])
interface IRoleWeb {
    @PostMapping
    fun save(
        @ModelAttribute
        @Valid
        t: RolePostDto,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @GetMapping("/{id}")
    fun findById(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @GetMapping
    fun findAll(
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @PutMapping("/{id}")
    fun update(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
        @ModelAttribute
        @Valid
        x: RolePutDto,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String
}

@Controller
@PreAuthorize("hasAuthority('admin')")
class RoleWebCtrl
    @Autowired
    constructor(
        private val authenticationFacade: AuthenticationFacade,
        private val service: IRoleService,
    ) : IRoleWeb {
        override fun save(
            t: RolePostDto,
            bindingResult: BindingResult,
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = throw NotImplementedError()

        override fun findById(
            id: String,
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = throw NotImplementedError()

        override fun findAll(
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = throw NotImplementedError()

        override fun update(
            id: String,
            x: RolePutDto,
            bindingResult: BindingResult,
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = throw NotImplementedError()

        override fun deleteById(
            id: String,
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = throw NotImplementedError()
    }
