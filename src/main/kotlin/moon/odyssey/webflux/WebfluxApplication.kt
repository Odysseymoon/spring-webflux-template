package moon.odyssey.webflux

import jakarta.annotation.PostConstruct
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import java.util.TimeZone

@SpringBootApplication
@ConfigurationPropertiesScan
class WebfluxApplication {
    companion object {
        init {
            java.security.Security.setProperty("networkaddress.cache.ttl", "10800")
        }
    }

    @PostConstruct
    fun started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(WebfluxApplication::class.java, *args)
}
