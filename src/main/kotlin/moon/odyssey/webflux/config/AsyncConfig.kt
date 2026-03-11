package moon.odyssey.webflux.config

import moon.odyssey.webflux.config.properties.AsyncProperties
import moon.odyssey.webflux.config.support.AsyncExceptionHandler
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig(
    private val properties: AsyncProperties
) : AsyncConfigurer {

    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = properties.size
        executor.maxPoolSize = properties.max
        executor.setKeepAliveSeconds(properties.keepAliveSeconds)
        executor.setQueueCapacity(properties.queueCapacity)
        executor.setThreadNamePrefix("Async-")
        executor.initialize()
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncExceptionHandler()
    }
}
