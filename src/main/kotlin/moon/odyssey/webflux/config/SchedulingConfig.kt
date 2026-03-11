package moon.odyssey.webflux.config

import moon.odyssey.webflux.config.properties.SchedulingProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.util.ErrorHandler

@Configuration
@EnableScheduling
class SchedulingConfig(
    private val properties: SchedulingProperties,
    private val errorHandler: ErrorHandler
) : SchedulingConfigurer {

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.poolSize = properties.size
        scheduler.setThreadNamePrefix("Task-Scheduler-")
        scheduler.setErrorHandler(errorHandler)
        scheduler.initialize()
        taskRegistrar.setTaskScheduler(scheduler)
    }
}
