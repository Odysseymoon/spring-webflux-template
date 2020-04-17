package moon.odyssey.webflux.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.util.ErrorHandler;

//@Profile("prod")
@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

    @Value("${schedule.threadPool.size:30}")
    private int poolSize;

    @Autowired
    @Qualifier("scheduledErrorHandler")
    private ErrorHandler errorHandler;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix("Task-Scheduler-");
        scheduler.setErrorHandler(errorHandler);
        scheduler.initialize();

        taskRegistrar.setTaskScheduler(scheduler);
    }

}
