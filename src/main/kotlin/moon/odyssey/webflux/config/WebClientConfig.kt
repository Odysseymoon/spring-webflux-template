package moon.odyssey.webflux.config

import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import moon.odyssey.webflux.utils.lambda.ThrowingConsumer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.LoggingCodecSupport
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig {

    private val log = LoggerFactory.getLogger(WebClientConfig::class.java)

    @Bean
    fun webClient(): WebClient {
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024 * 50) }
            .build()

        exchangeStrategies.messageWriters().stream()
            .filter { writer -> writer is LoggingCodecSupport }
            .forEach { writer -> (writer as LoggingCodecSupport).setEnableLoggingRequestDetails(true) }

        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .secure(
                            ThrowingConsumer.unchecked { sslContextSpec ->
                                sslContextSpec.sslContext(
                                    SslContextBuilder.forClient()
                                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                        .build()
                                )
                            }
                        )
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120_000)
                        .responseTimeout(Duration.ofSeconds(180))
                        .doOnConnected { conn ->
                            conn.addHandlerLast(ReadTimeoutHandler(180))
                                .addHandlerLast(WriteTimeoutHandler(180))
                        }
                )
            )
            .exchangeStrategies(exchangeStrategies)
            .filter(
                ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
                    log.debug("Request: {} {}", clientRequest.method(), clientRequest.url())
                    clientRequest.headers().forEach { (name, values) ->
                        values.forEach { value -> log.debug("{} : {}", name, value) }
                    }
                    Mono.just(clientRequest)
                }
            )
            .filter(
                ExchangeFilterFunction.ofResponseProcessor { clientResponse ->
                    clientResponse.headers().asHttpHeaders().forEach { (name, values) ->
                        values.forEach { value -> log.debug("{} : {}", name, value) }
                    }
                    Mono.just(clientResponse)
                }
            )
            .defaultHeader(
                "user-agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.3"
            )
            .build()
    }
}
