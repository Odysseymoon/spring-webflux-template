package moon.odyssey.webflux.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "async.thread-pool")
data class AsyncProperties(
    val size: Int = 20,
    val max: Int = 100,
    val keepAliveSeconds: Int = 60,
    val queueCapacity: Int = 1000
)
