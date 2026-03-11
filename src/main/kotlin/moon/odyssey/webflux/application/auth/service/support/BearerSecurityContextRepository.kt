package moon.odyssey.webflux.application.auth.service.support

import moon.odyssey.webflux.utils.reactive.RxUtil
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class BearerSecurityContextRepository(
    private val authenticationManager: ReactiveAuthenticationManager
) : ServerSecurityContextRepository {

    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> {
        return Mono.empty()
    }

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        return RxUtil.elasticMono {
            val token = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
                ?.takeIf { it.startsWith("Bearer ") }
                ?.substring(7)
            token?.let { Mono.just(it) } ?: Mono.empty()
        }
            .switchIfEmpty(
                Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not found token, Please signIn again."))
            )
            .map { token -> UsernamePasswordAuthenticationToken(token, token) }
            .flatMap(authenticationManager::authenticate)
            .map { SecurityContextImpl(it) }
    }
}
