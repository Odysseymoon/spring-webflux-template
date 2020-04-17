package moon.odyssey.webflux.config.support;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {

        log.error("Async Exception occurred at {} ", method.getName());
        log.error(ex.getLocalizedMessage(), ex);
    }

}
