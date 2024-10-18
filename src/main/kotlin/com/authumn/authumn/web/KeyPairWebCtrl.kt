package com.authumn.authumn.web

import com.authumn.authumn.commons.AuthenticationFacade
import com.authumn.authumn.keypairs.IKeyPairService
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@RequestMapping(value = ["/keypair"], produces = [MediaType.TEXT_HTML_VALUE])
interface IKeyPairWeb {
    @PostMapping
    fun generate(
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

    @DeleteMapping("/{id}")
    fun deleteById(
        @PathVariable
        @NotBlank(message = "id must not be blank")
        id: String,
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String

    @DeleteMapping
    fun deleteAllButNewest(
        model: Model,
        redirectAttributes: RedirectAttributes,
    ): String
}

@Controller
@PreAuthorize("hasAuthority('admin')")
class KeyPairWebCtrl
    @Autowired
    constructor(
        private val authenticationFacade: AuthenticationFacade,
        private val service: IKeyPairService,
    ) : IKeyPairWeb {
        override fun generate(
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

        override fun deleteById(
            id: String,
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = throw NotImplementedError()

        override fun deleteAllButNewest(
            model: Model,
            redirectAttributes: RedirectAttributes,
        ): String = throw NotImplementedError()
    }
