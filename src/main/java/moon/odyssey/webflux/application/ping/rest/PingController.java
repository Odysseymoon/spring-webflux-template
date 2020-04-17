package moon.odyssey.webflux.application.ping.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/api/ping")
@Slf4j
public class PingController {

    @GetMapping
    public Mono<String> ping() {

        return Mono.just("pong");
    }
}
