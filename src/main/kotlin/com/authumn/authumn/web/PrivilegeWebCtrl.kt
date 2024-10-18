package com.authumn.authumn.web

import com.authumn.authumn.commons.AuthenticationFacade
import com.authumn.authumn.privileges.IPrivilegeService
import com.authumn.authumn.privileges.PrivilegePostDto
import com.authumn.authumn.privileges.PrivilegePutDto
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

@RequestMapping(value = ["/privilege"], produces = [MediaType.TEXT_HTML_VALUE])
interface IPrivilegeWeb {
    @PostMapping
    fun save(
        @ModelAttribute
        @Valid
        t: PrivilegePostDto,
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
        x: PrivilegePutDto,
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
class PrivilegeWebCtrl
    @Autowired
    constructor(
        private val authenticationFacade: AuthenticationFacade,
        private val service: IPrivilegeService,
    ) : IPrivilegeWeb {
        override fun save(
            t: PrivilegePostDto,
            bindingResult: BindingResult,
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String {
            try {
                if (bindingResult.hasErrors()) {
                    throw Throwable(message = bindingResult.allErrors.map { it.defaultMessage }.joinToString { "$it" })
                }
                service.save(t).toDto()
            } catch (ex: Throwable) {
                redirectAttributes.addFlashAttribute("error", ex.message)
            }
            return "redirect:/privilege"
        }

        override fun findById(
            id: String,
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = throw NotImplementedError()

        override fun findAll(
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String {
            try {
                model.addAttribute("principal", authenticationFacade.getPrincipal())
                model.addAttribute("privileges", service.findAll().map { it.toDto() })
            } catch (ex: Throwable) {
                model.addAttribute("error", ex.message)
            }
            return "privilege"
        }

        override fun update(
            id: String,
            x: PrivilegePutDto,
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = throw NotImplementedError()

        override fun deleteById(
            id: String,
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String {
            try {
                service.deleteById(id)
            } catch (ex: Throwable) {
                redirectAttributes.addFlashAttribute("error", ex.message)
            }
            return "redirect:/privilege"
        }
    }
