package moon.odyssey.webflux.application.advice

import moon.odyssey.webflux.utils.exception.AppBaseException
import moon.odyssey.webflux.utils.exception.AppErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import java.util.Locale

@RestControllerAdvice
class GlobalAdvice(
    private val messageSource: MessageSource
) {

    private val log = LoggerFactory.getLogger(GlobalAdvice::class.java)

    @ExceptionHandler(AppBaseException::class)
    fun handleAppBaseException(
        ex: AppBaseException,
        locale: Locale,
        exchange: ServerWebExchange
    ): ResponseEntity<AppErrorResponse> {
        if (ex.cause != null) {
            log.error(ex.localizedMessage, ex)
        }

        val status = ex.status
        val errorMessage = ex.errorCode?.let { messageSource.getMessage(it.value, ex.args, locale) }

        val response = AppErrorResponse(
            timestamp = System.currentTimeMillis(),
            path = exchange.request.path.value(),
            status = status.value(),
            error = status.reasonPhrase,
            message = errorMessage ?: ex.localizedMessage,
            requestId = exchange.request.id
        )

        return ResponseEntity(response, status)
    }
}
