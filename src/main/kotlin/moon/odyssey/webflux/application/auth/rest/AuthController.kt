package moon.odyssey.webflux.application.auth.rest

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import moon.odyssey.webflux.application.auth.entity.User
import moon.odyssey.webflux.application.auth.model.TokenInfo
import moon.odyssey.webflux.application.auth.model.UserParam
import moon.odyssey.webflux.application.auth.service.TokenService
import moon.odyssey.webflux.application.auth.service.UserService
import moon.odyssey.webflux.utils.reactive.RxUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
@RequestMapping(path = ["/api/auth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthController(
    private val userService: UserService,
    private val tokenService: TokenService,
    private val passwordEncoder: PasswordEncoder
) {

    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping(path = ["/signup"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@RequestBody @Valid userInfo: UserParam): Mono<Void> {
        return userService.findById(userInfo.userId)
            .flatMap {
                Mono.error<User>(
                    ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "The userId already exists : ${userInfo.userId}"
                    )
                )
            }
            .switchIfEmpty(userService.addUser(userInfo.userId, passwordEncoder.encode(userInfo.password)))
            .then()
    }

    @PostMapping(path = ["/signin"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun signIn(@RequestBody @Valid userInfo: UserParam): Mono<TokenInfo> {
        return userService.findById(userInfo.userId)
            .filter { user -> passwordEncoder.matches(userInfo.password, user.password) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.BAD_REQUEST, "Not found user info or invalid password")))
            .flatMap { user -> RxUtil.elasticMono { Mono.just(tokenService.createToken(user)) } }
    }

    @GetMapping("/refresh")
    @PreAuthorize("hasAuthority('refresh')")
    @SecurityRequirement(name = "bearerAuth")
    fun refresh(@Parameter(hidden = true) @AuthenticationPrincipal userInfo: UserDetails?): Mono<TokenInfo> {
        log.info("##### user {}", userInfo)

        return Mono.justOrEmpty(userInfo)
            .map { it.username }
            .flatMap(userService::findById)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not found token, Please signin again.")))
            .flatMap { user -> RxUtil.elasticMono { Mono.just(tokenService.createToken(user)) } }
    }
}
