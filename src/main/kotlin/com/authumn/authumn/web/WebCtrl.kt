package com.authumn.authumn.web

import com.authumn.authumn.commons.AuthenticationFacade
import com.authumn.authumn.commons.ErrorDto
import com.authumn.authumn.users.KustomUser
import com.authumn.authumn.users.UserGetDto
import com.authumn.authumn.users.UserPostDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Controller
@RequestMapping(value = ["/"], produces = [MediaType.TEXT_HTML_VALUE])
class WebCtrl
    @Autowired
    constructor(
        private val authenticationFacade: AuthenticationFacade,
        private val webClient: WebClient,
    ) {
        @GetMapping(path = ["", "/index"])
        fun index(model: Model): String {
            model.addAttribute("user", (authenticationFacade.authentication?.principal as KustomUser))
            return "index"
        }

        @GetMapping("/login")
        fun login(): String = "login"

        @GetMapping("/logout")
        fun logout(): String = "logout"

        @GetMapping("/signup")
        fun signup(): String = "signup"

        @PostMapping("/signup")
        fun pSignup(
            @ModelAttribute dto: UserPostDto,
            model: Model,
        ): String {
            webClient
                .post()
                .uri("/api/user")
                .bodyValue(dto)
                .exchangeToMono {
                    when {
                        it.statusCode().is2xxSuccessful -> it.bodyToMono(UserGetDto::class.java)
                        it.statusCode().isError -> it.bodyToMono(ErrorDto::class.java)
                        else -> Mono.empty()
                    }
                }.block()
                .let {
                    when (it) {
                        is UserGetDto -> {
                            model.addAttribute("user", it)
                            return "redirect:/login?created"
                        }
                        is ErrorDto -> {
                            model.addAttribute("error", it)
                            return "signup"
                        }
                        else -> {
                            model.addAttribute("error", it)
                            return "signup"
                        }
                    }
                }
        }

        @GetMapping("/fragments")
        fun getFragments(): String = "fragments"
    }
