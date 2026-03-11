package moon.odyssey.webflux

import moon.odyssey.webflux.utils.exception.AppBaseException
import moon.odyssey.webflux.utils.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(path = ["api/test"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TestController {

    @GetMapping("/error")
    fun appError(): Mono<String> {
        return Mono.error { AppBaseException(HttpStatus.BAD_REQUEST, ErrorCode.BASE_CODE) }
    }
}
