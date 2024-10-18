package com.authumn.authumn.web

import com.authumn.authumn.commons.AuthenticationFacade
import com.authumn.authumn.users.IUserService
import com.authumn.authumn.users.UserPostDto
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@RequestMapping(value = ["/"], produces = [MediaType.TEXT_HTML_VALUE])
interface IWeb {
    @GetMapping(path = ["", "/index"])
    fun index(
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @GetMapping("/login")
    fun login(
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @GetMapping("/logout")
    fun logout(
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @GetMapping("/signup")
    fun signup(
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @PostMapping("/signup")
    fun signup(
        @ModelAttribute
        @Valid
        t: UserPostDto,
        bindingResult: BindingResult,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @GetMapping("/fragments")
    fun getFragments(
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String
}

@Controller
class WebCtrl
    @Autowired
    constructor(
        private val authenticationFacade: AuthenticationFacade,
        private val service: IUserService,
    ) : IWeb {
        override fun index(
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String {
            model.addAttribute("principal", authenticationFacade.getPrincipal())
            return "index"
        }

        override fun login(
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = "login"

        override fun logout(
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = "logout"

        override fun signup(
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = "signup"

        override fun signup(
            t: UserPostDto,
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
                model.addAttribute("error", ex.message)
                return "signup"
            }
            return "redirect:/login?created"
        }

        override fun getFragments(
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = "fragments"
    }
