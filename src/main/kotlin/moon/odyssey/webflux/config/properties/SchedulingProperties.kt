package moon.odyssey.webflux.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "schedule.thread-pool")
data class SchedulingProperties(
    val size: Int = 30
)
