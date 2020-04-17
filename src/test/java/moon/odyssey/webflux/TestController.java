package moon.odyssey.webflux;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import moon.odyssey.webflux.utils.exception.AppBaseException;
import moon.odyssey.webflux.utils.exception.ErrorCode;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "api/test", produces = MediaType.APPLICATION_JSON_VALUE)
public class TestController {

    @GetMapping("/error")
    public Mono<String> appError() {
        return Mono.error(() -> new AppBaseException(HttpStatus.BAD_REQUEST, ErrorCode.BASE_CODE));
    }

}
