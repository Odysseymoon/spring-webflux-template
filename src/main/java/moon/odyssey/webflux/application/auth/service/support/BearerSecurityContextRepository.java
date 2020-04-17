package moon.odyssey.webflux.application.auth.service.support;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import moon.odyssey.webflux.utils.reactive.RxUtil;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BearerSecurityContextRepository implements ServerSecurityContextRepository {

    private final ReactiveAuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {

        return
            RxUtil.elasticMono(() -> Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                                             .filter(s -> s.startsWith("Bearer "))
                                             .map(s -> s.substring(7))
                                             .map(Mono::just)
                                             .orElseGet(Mono::empty)
                  )
                  .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not found token, Please signIn again.")))
                  .map(token -> new UsernamePasswordAuthenticationToken(token, token))
                  .flatMap(authenticationManager::authenticate)
                  .map(SecurityContextImpl::new)
                  ;
    }
}
