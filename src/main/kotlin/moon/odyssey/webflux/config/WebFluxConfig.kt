package moon.odyssey.webflux.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

@Configuration
class WebFluxConfig(
    @Value("\${spring.datasource.hikari.maximum-pool-size:10}")
    private val connectionPoolSize: Int
) {

    @Bean("jdbcScheduler")
    fun jdbcScheduler(): Scheduler {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize))
    }

    @Bean
    fun transactionTemplate(transactionManager: PlatformTransactionManager): TransactionTemplate {
        return TransactionTemplate(transactionManager)
    }
}
