package moon.odyssey.webflux.application.ping.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(path = ["/api/ping"])
class PingController {

    @GetMapping
    fun ping(): Mono<String> = Mono.just("pong")
}
