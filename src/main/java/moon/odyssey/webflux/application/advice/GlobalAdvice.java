package moon.odyssey.webflux.application.advice;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moon.odyssey.webflux.utils.exception.AppBaseException;
import moon.odyssey.webflux.utils.exception.AppErrorResponse;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalAdvice {

    private final MessageSource messageSource;

    @ExceptionHandler({AppBaseException.class})
    public ResponseEntity<AppErrorResponse> handleAppBaseException(AppBaseException ex, Locale locale, ServerWebExchange exchange) {

        if(ex.getCause() != null) {
            log.error(ex.getLocalizedMessage(), ex);
        }

        HttpStatus status = ex.getStatus() != null
                            ? ex.getStatus()
                            : HttpStatus.BAD_REQUEST;

        String errorMessage = ex.getErrorCode() != null
                              ? messageSource.getMessage(ex.getErrorCode().getValue(), ex.getArgs(), locale)
                              : null;

        return new ResponseEntity<>(
            AppErrorResponse.builder()
                            .timestamp(System.currentTimeMillis())
                            .path(exchange.getRequest().getPath().value())
                            .status(status.value())
                            .error(status.getReasonPhrase())
                            .message(errorMessage != null ? errorMessage : ex.getLocalizedMessage())
                            .requestId(exchange.getRequest().getId())
                            .build()
            , status
        );


    }

}
