package moon.odyssey.webflux.application.auth.service.support

import moon.odyssey.webflux.application.auth.service.TokenService
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Component
class AuthenticationJwtManager(
    private val tokenService: TokenService
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .map { it.credentials.toString() }
            .map { tokenService.verifyToken(it) }
            .map<Authentication> { decoded ->
                val authorities = decoded.getClaim("scopes")
                    .asList(String::class.java)
                    .map { SimpleGrantedAuthority(it) }
                UsernamePasswordAuthenticationToken(
                    User.builder()
                        .username(decoded.subject)
                        .password("Noop")
                        .authorities(authorities)
                        .build(),
                    null,
                    authorities
                )
            }
            .onErrorResume { throwable ->
                Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, throwable.localizedMessage))
            }
    }
}
