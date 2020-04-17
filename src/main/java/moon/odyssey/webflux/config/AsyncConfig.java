package moon.odyssey.webflux.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import moon.odyssey.webflux.config.support.AsyncExceptionHandler;

@Configuration
@EnableAsync
public class AsyncConfig extends AsyncConfigurerSupport {

    @Value("${async.threadPool.size:20}")
    private int poolSize;

    @Value("${async.threadPool.max:100}")
    private int maxPoolSize;

    @Value("${async.threadPool.keepAliveSeconds:60}")
    private int keepAliveSeconds;

    @Value("${async.threadPool.queueCapacity:1000}")
    private int queueCapacity;

    @Override
    public Executor getAsyncExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

}
