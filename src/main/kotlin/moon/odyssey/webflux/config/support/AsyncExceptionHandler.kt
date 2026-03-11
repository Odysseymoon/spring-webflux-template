package moon.odyssey.webflux.config.support

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import java.lang.reflect.Method

class AsyncExceptionHandler : AsyncUncaughtExceptionHandler {

    private val log = LoggerFactory.getLogger(AsyncExceptionHandler::class.java)

    override fun handleUncaughtException(ex: Throwable, method: Method, vararg params: Any) {
        log.error("Async Exception occurred at {} ", method.name)
        log.error(ex.localizedMessage, ex)
    }
}
