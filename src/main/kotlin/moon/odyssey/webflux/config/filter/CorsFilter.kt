package moon.odyssey.webflux.config.filter

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class CorsFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val headers = exchange.response.headers
        headers.add("Access-Control-Allow-Origin", "*")
        headers.add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS")
        headers.add(
            "Access-Control-Allow-Headers",
            "Authorization,DNT,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range"
        )
        return if (exchange.request.method == HttpMethod.OPTIONS) {
            headers.add("Access-Control-Max-Age", "1728000")
            exchange.response.statusCode = HttpStatus.NO_CONTENT
            Mono.empty()
        } else {
            headers.add(
                "Access-Control-Expose-Headers",
                "Authorization,DNT,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range"
            )
            chain.filter(exchange)
        }
    }
}
