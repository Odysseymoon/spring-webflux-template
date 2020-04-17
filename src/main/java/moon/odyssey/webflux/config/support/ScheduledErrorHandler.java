package moon.odyssey.webflux.config.support;

import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Component("scheduledErrorHandler")
@Slf4j
public class ScheduledErrorHandler implements ErrorHandler {

    @Override
    public void handleError(Throwable t) {

        log.error(t.getLocalizedMessage(), t);
    }
}
