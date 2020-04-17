package moon.odyssey.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class WebfluxApplication {

    static {
        java.security.Security.setProperty ("networkaddress.cache.ttl" , "10800"); // DNS Cache 3 hours
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(WebfluxApplication.class, args);
    }
}
