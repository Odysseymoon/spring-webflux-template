package moon.odyssey.webflux.application.auth.service.support;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import moon.odyssey.webflux.application.auth.service.TokenService;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthenticationJwtManager implements ReactiveAuthenticationManager {

    private final TokenService tokenService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        return
            Mono.just(authentication)
                .map(auth -> auth.getCredentials().toString())
                .flatMap(token -> Mono.just(tokenService.verifyToken(token)))
                .flatMap(decodedJWT -> Mono.just(decodedJWT)
                                           .map(jwt -> jwt.getClaim("scopes").asList(String.class).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
                                           .map(authorities ->
                                                new UsernamePasswordAuthenticationToken(
                                                    User.builder()
                                                        .username(decodedJWT.getSubject())
                                                        .password("Noop")
                                                        .authorities(authorities)
                                                        .build()
                                                    , null
                                                    , authorities
                                                    )
                                            )
                )
                .onErrorResume(throwable -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, throwable.getLocalizedMessage())))
                .flatMap(Mono::just)
            ;
    }
}
