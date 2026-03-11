package moon.odyssey.webflux.config

import moon.odyssey.webflux.application.auth.service.support.AuthenticationJwtManager
import moon.odyssey.webflux.application.auth.service.support.BearerSecurityContextRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val authenticationManager: AuthenticationJwtManager,
    private val securityContextRepository: BearerSecurityContextRepository
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .exceptionHandling { handling ->
                handling.authenticationEntryPoint { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    exchange.response.setComplete()
                }.accessDeniedHandler { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.FORBIDDEN
                    exchange.response.setComplete()
                }
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .cors { it.disable() }
            .csrf { it.disable() }
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/api/ping/**", "/api/test/**", "/api/auth/sign**").permitAll()
                    .pathMatchers(HttpMethod.OPTIONS).permitAll()
                    .pathMatchers("/api/**").authenticated()
                    .anyExchange().permitAll()
            }
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }
}
