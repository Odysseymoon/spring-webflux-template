package moon.odyssey.webflux.config.support

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.ErrorHandler

@Component("scheduledErrorHandler")
class ScheduledErrorHandler : ErrorHandler {

    private val log = LoggerFactory.getLogger(ScheduledErrorHandler::class.java)

    override fun handleError(t: Throwable) {
        log.error(t.localizedMessage, t)
    }
}
