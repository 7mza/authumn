package com.authumn.authumn.web

import com.authumn.authumn.commons.AuthenticationFacade
import com.authumn.authumn.users.IUserService
import com.authumn.authumn.users.UserPostDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping(value = ["/"], produces = [MediaType.TEXT_HTML_VALUE])
interface IWeb {
    @GetMapping(path = ["", "/index"])
    fun index(model: Model): String

    @GetMapping("/login")
    fun login(model: Model): String

    @GetMapping("/logout")
    fun logout(model: Model): String

    @GetMapping("/signup")
    fun signup(model: Model): String

    @PostMapping("/signup")
    fun signup(
        t: UserPostDto,
        model: Model,
    ): String

    @GetMapping("/fragments")
    fun getFragments(model: Model): String
}

@Controller
class WebCtrl
    @Autowired
    constructor(
        private val authenticationFacade: AuthenticationFacade,
        private val service: IUserService,
    ) : IWeb {
        override fun index(model: Model): String {
            model.addAttribute("principal", authenticationFacade.getPrincipal())
            return "index"
        }

        override fun login(model: Model): String = "login"

        override fun logout(model: Model): String = "logout"

        override fun signup(model: Model): String = "signup"

        override fun signup(
            t: UserPostDto,
            model: Model,
        ): String {
            try {
                model.addAttribute("user", service.save(t).toDto())
            } catch (ex: Throwable) {
                model.addAttribute("error", ex.message)
                return "signup"
            }
            return "redirect:/login?created"
        }

        override fun getFragments(model: Model): String = "fragments"
    }
