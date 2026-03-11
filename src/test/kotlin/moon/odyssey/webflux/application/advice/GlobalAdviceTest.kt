package moon.odyssey.webflux.application.advice

import moon.odyssey.webflux.utils.exception.AppErrorResponse
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.MethodName::class)
class GlobalAdviceTest {

    private val log = LoggerFactory.getLogger(GlobalAdviceTest::class.java)

    @Autowired
    private lateinit var testClient: WebTestClient

    @Test
    fun _1_testError_Should_Return_AppErrorResponse() {
        testClient
            .get()
            .uri("/api/test/error")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(AppErrorResponse::class.java)
            .consumeWith { result -> log.info(result.toString()) }
    }

    @Test
    fun _2_testErrorWithQueryParam_Should_Return_AppErrorResponse() {
        testClient
            .get()
            .uri("/api/test/error?lang=en-US")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(AppErrorResponse::class.java)
            .consumeWith { result -> log.info(result.toString()) }
    }

    @Test
    fun _3_testErrorWithHeader_Should_Return_AppErrorResponse() {
        testClient
            .get()
            .uri("/api/test/error")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(AppErrorResponse::class.java)
            .consumeWith { result -> log.info(result.toString()) }
    }
}
